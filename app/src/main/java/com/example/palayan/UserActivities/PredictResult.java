package com.example.palayan.UserActivities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import com.example.palayan.R;
import com.example.palayan.databinding.ActivityPredictResultBinding;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PredictResult extends AppCompatActivity {

    private ActivityPredictResultBinding root;
    private Bitmap capturedBitmap;
    private LoadingDialog loadingDialog;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String deviceId;

    // Store prediction info
    private String diseaseName, scientificName, description, symptoms, causes, treatments;
    private String imageUrl;
    private String imagePath;
    private boolean isFromStage2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityPredictResultBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        loadingDialog = new LoadingDialog(this);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        imagePath = getIntent().getStringExtra("imagePath");
        isFromStage2 = getIntent().getBooleanExtra("isFromStage2", false);

        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                capturedBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                root.ivDiseaseImage.setImageBitmap(capturedBitmap);

                if (isFromStage2) {
                    // Data already provided from Stage 2
                    displayStage2Results();
                } else {
                    Toast.makeText(this, "No prediction data available", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image path passed", Toast.LENGTH_SHORT).show();
        }

        root.btnBack.setOnClickListener(view -> onBackPressed());
        root.btnSave.setOnClickListener(v -> showSaveDialog());
    }

    private void displayStage2Results() {
        // Get data from Stage 2
        diseaseName = getIntent().getStringExtra("diseaseName");
        scientificName = getIntent().getStringExtra("scientificName");
        description = getIntent().getStringExtra("description");
        symptoms = getIntent().getStringExtra("symptoms");
        causes = getIntent().getStringExtra("cause");
        treatments = getIntent().getStringExtra("treatments");
        float confidence = getIntent().getFloatExtra("confidence", 0.0f);

        // Display results
        root.tvDiseaseName.setText(diseaseName);
        root.tvSciName.setText(scientificName);

        if (description != null) {
            String formattedDesc = formatText(description);
            root.tvDiseaseDesc.setText(formattedDesc);
        }

        if (symptoms != null) {
            root.tvSymptoms.setText(formatText(symptoms));
        }

        if (treatments != null) {
            root.tvTreatments.setText(formatText(treatments));
        }

        if (causes != null) {
            String formattedCause = formatText(causes);
            root.tvCause.setText(formattedCause);
        }

        String confidenceText = "Confidence: " + String.format("%.1f%%", confidence * 100);
        Toast.makeText(this, confidenceText, Toast.LENGTH_LONG).show();
    }

    // Custom dialog using res/layout/dialog_save_result.xml
    private void showSaveDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_save_result);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        ImageView ivClose = dialog.findViewById(R.id.ivClose);
        View cvSaveOnly = dialog.findViewById(R.id.cvSaveOnly);
        View cvAddNotes = dialog.findViewById(R.id.cvAddNotes);

        ivClose.setOnClickListener(v -> dialog.dismiss());

        //upload image first, then save to Firestore
        cvSaveOnly.setOnClickListener(v -> {
            uploadImage(() -> {
                savePrediction();
                dialog.dismiss();
            });
        });

        // Add Notes: DO NOT upload/save now. Just pass diseaseName + imagePath
        cvAddNotes.setOnClickListener(v -> {
            Intent intent = new Intent(PredictResult.this, TreatmentNotes.class);
            intent.putExtra("diseaseName", diseaseName);
            intent.putExtra("scientificName", scientificName);
            intent.putExtra("description", description);
            intent.putExtra("symptoms", symptoms);
            intent.putExtra("causes", causes);
            intent.putExtra("treatments", treatments);
            intent.putExtra("imagePath", imagePath); // let TreatmentNotes handle upload later if needed
            intent.putExtra("deviceId", deviceId);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();

        // Make dialog full width
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void uploadImage(Runnable onSuccess) {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        loadingDialog.show("Uploading image...");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageData = baos.toByteArray();

            String filename = "prediction_" + deviceId + "_" + UUID.randomUUID() + ".jpg";
            String path = "predictions/" + deviceId + "/" + filename;

            StorageReference imageRef = storage.getReference().child(path);
            UploadTask uploadTask = imageRef.putBytes(imageData);

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
                    onSuccess.run();
                }).addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to get image URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                loadingDialog.dismiss();
                Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            loadingDialog.dismiss();
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String formatText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        if (text.contains("•") || text.contains("-") || text.contains("*") ||
                text.contains("1.") || text.contains("2.") || text.contains("3.")) {
            return text;
        }
        if (text.contains(".") && text.split("\\.").length > 2) {
            String[] sentences = text.split("\\.");
            StringBuilder formatted = new StringBuilder();
            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (!sentence.isEmpty()) {
                    formatted.append("• ").append(sentence).append(".\n");
                }
            }
            return formatted.toString();
        }
        return text;
    }

    // Save using Firestore server time and UUID (no device millis)
    private void savePrediction() {
        if (diseaseName == null || diseaseName.isEmpty()) {
            Toast.makeText(this, "No prediction to save yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String documentId = deviceId + "_" + UUID.randomUUID();

        Map<String, Object> predictionData = new HashMap<>();
        predictionData.put("diseaseName", diseaseName);
        predictionData.put("scientificName", scientificName);
        predictionData.put("description", description);
        predictionData.put("symptoms", symptoms);
        predictionData.put("causes", causes);
        predictionData.put("treatments", treatments);
        predictionData.put("deviceId", deviceId);
        predictionData.put("imageUrl", imageUrl);
        predictionData.put("documentId", documentId.toString());
        predictionData.put("timestamp", FieldValue.serverTimestamp());

        loadingDialog.show("Saving result...");

        firestore.collection("users")
                .document(deviceId)
                .collection("predictions_result")
                .document(documentId.toString())
                .set(predictionData)
                .addOnSuccessListener(aVoid -> {
                    loadingDialog.dismiss();
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    loadingDialog.dismiss();
                    Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Success!");
        builder.setMessage("Your prediction result has been saved successfully with the image!");
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            positiveButton.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}