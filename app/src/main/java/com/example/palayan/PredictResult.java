package com.example.palayan;

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

import com.example.palayan.API.ApiClient;
import com.example.palayan.API.ApiService;
import com.example.palayan.API.PredictResponse;
import com.example.palayan.UserActivities.LoadingDialog;
import com.example.palayan.UserActivities.TreatmentNotes;
import com.example.palayan.databinding.ActivityPredictResultBinding;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictResult extends AppCompatActivity {

    private ActivityPredictResultBinding root;
    private Bitmap capturedBitmap;
    private LoadingDialog loadingDialog;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private String deviceId;

    // Store prediction info
    private String diseaseName, description, symptoms, causes, treatments;
    private String imageUrl;

    // Keep raw image path to pass to Add Notes (no upload yet)
    private String imagePath;

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
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                capturedBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                root.ivDiseaseImage.setImageBitmap(capturedBitmap);
                callPredictionAPI();
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image path passed", Toast.LENGTH_SHORT).show();
        }

        root.btnBack.setOnClickListener(view -> onBackPressed());
        root.btnSave.setOnClickListener(v -> showSaveDialog());
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

    private void callPredictionAPI() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to predict", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isFinishing()) loadingDialog.show("Loading result...");
        callPredictionAPIWithRetry(0);
    }

    private void callPredictionAPIWithRetry(int retryCount) {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to predict", Toast.LENGTH_SHORT).show();
            return;
        }

        final int MAX_RETRIES = 100;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();

            if (loadingDialog != null) {
                String msg = retryCount == 0
                        ? "Loading result..."
                        : "Loading result... Attempt " + (retryCount + 1) + "/100";
                loadingDialog.setMessage(msg);
            }

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("image/jpeg"), imageBytes);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "image", "image.jpg", requestFile);

            ApiService apiService = ApiClient.getApiService();
            Call<PredictResponse> call = apiService.predictDisease(imagePart);

            call.enqueue(new Callback<PredictResponse>() {
                @Override
                public void onResponse(Call<PredictResponse> call, Response<PredictResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        if (loadingDialog != null) loadingDialog.dismiss();
                        PredictResponse result = response.body();
                        displayPredictionResult(result);
                    } else {
                        String errorBody = "";
                        try {
                            errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        } catch (Exception e) {
                            errorBody = "Could not read error body";
                        }

                        if (retryCount < MAX_RETRIES) {
                            new android.os.Handler().postDelayed(() -> {
                                callPredictionAPIWithRetry(retryCount + 1);
                            }, 2000);
                        } else {
                            if (loadingDialog != null) loadingDialog.dismiss();
                            Toast.makeText(PredictResult.this,
                                    "API Error: " + response.code() + " - " + errorBody,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<PredictResponse> call, Throwable t) {
                    String errorMessage = "Network Error: " + t.getMessage();

                    if (retryCount < MAX_RETRIES) {
                        if (loadingDialog != null) {
                            String msg = "Network error, retrying... Attempt " + (retryCount + 2) + "/3";
                            loadingDialog.setMessage(msg);
                        }
                        new android.os.Handler().postDelayed(() -> {
                            callPredictionAPIWithRetry(retryCount + 1);
                        }, 2000);
                    } else {
                        if (loadingDialog != null) loadingDialog.dismiss();
                        Toast.makeText(PredictResult.this, errorMessage, Toast.LENGTH_LONG).show();

                        if (t instanceof java.net.UnknownHostException) {
                            Toast.makeText(PredictResult.this, "Cannot connect to server. Check your internet connection.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof java.net.ConnectException) {
                            Toast.makeText(PredictResult.this, "Server is not responding. Please try again later.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof javax.net.ssl.SSLException) {
                            Toast.makeText(PredictResult.this, "SSL connection error. Please check your network settings.", Toast.LENGTH_LONG).show();
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            Toast.makeText(PredictResult.this, "Request timeout. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });

        } catch (Exception e) {
            if (loadingDialog != null) loadingDialog.dismiss();
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void displayPredictionResult(PredictResponse result) {
        try {
            diseaseName = result.predicted_disease;
            description = result.disease_info.description;
            causes = result.disease_info.cause;

            root.tvDiseaseName.setText(diseaseName);
            root.tvSciName.setText(result.disease_info.scientific_name);

            if (result.disease_info.description != null) {
                String formattedDesc = formatText(result.disease_info.description);
                root.tvDiseaseDesc.setText(formattedDesc);
            }

            StringBuilder symptomsText = new StringBuilder();
            if (result.disease_info.symptoms != null) {
                if (result.disease_info.symptoms instanceof String) {
                    String symptomsStr = (String) result.disease_info.symptoms;
                    symptomsText.append(formatText(symptomsStr));
                } else if (result.disease_info.symptoms instanceof List) {
                    List<?> symptomsList = (List<?>) result.disease_info.symptoms;
                    for (Object symptom : symptomsList) {
                        symptomsText.append("• ").append(symptom.toString()).append("\n");
                    }
                }
            }
            symptoms = symptomsText.toString();
            root.tvSymptoms.setText(symptoms);

            StringBuilder treatmentsText = new StringBuilder();
            if (result.disease_info.treatments != null) {
                if (result.disease_info.treatments instanceof String) {
                    String treatmentsStr = (String) result.disease_info.treatments;
                    treatmentsText.append(formatText(treatmentsStr));
                } else if (result.disease_info.treatments instanceof List) {
                    List<?> treatmentsList = (List<?>) result.disease_info.treatments;
                    for (Object treatment : treatmentsList) {
                        treatmentsText.append("• ").append(treatment.toString()).append("\n");
                    }
                }
            }
            treatments = treatmentsText.toString();
            root.tvTreatments.setText(treatments);

            if (result.disease_info.cause != null) {
                String formattedCause = formatText(result.disease_info.cause);
                root.tvCause.setText(formattedCause);
            }

            String confidenceText = "Confidence: " + String.format("%.1f%%", result.confidence * 100);
            Toast.makeText(this, confidenceText, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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