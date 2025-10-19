package com.example.palayan.UserActivities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
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
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityPredictResultBinding;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.tensorflow.lite.Interpreter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
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

    // Add offline model manager
    private OfflineModelManager offlineModelManager;

    // Store prediction info
    private String diseaseName, scientificName, description, symptoms, causes, treatments;
    private String imageUrl;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityPredictResultBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        loadingDialog = new LoadingDialog(this);
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize offline model
        offlineModelManager = new OfflineModelManager(this);

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                capturedBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                root.ivDiseaseImage.setImageBitmap(capturedBitmap);

                // Wait for offline data to load, then perform prediction
                waitForOfflineDataAndPredict();
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image path passed", Toast.LENGTH_SHORT).show();
        }

        root.btnBack.setOnClickListener(view -> onBackPressed());
        root.btnSave.setOnClickListener(v -> showSaveDialog());
    }

    // Wait for offline data to load
    private void waitForOfflineDataAndPredict() {
        if (offlineModelManager.isDataReady()) {
            performPrediction();
        } else {
            // Wait for data to load
            new Thread(() -> {
                while (!offlineModelManager.isDataReady()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

                runOnUiThread(() -> {
                    if (offlineModelManager.isDataReady()) {
                        performPrediction();
                    } else {
                        Toast.makeText(this, "Failed to load offline data", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        }
    }

    // Smart prediction method
    private void performPrediction() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to predict", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check network availability
        if (NetworkUtils.isNetworkAvailable(this) && NetworkUtils.isInternetAvailable()) {
            // Online prediction
            performOnlinePrediction();
        } else {
            // Offline prediction
            performOfflinePrediction();
        }
    }

    // Online prediction method
    private void performOnlinePrediction() {
        if (!isFinishing()) {
            loadingDialog.show("Loading result (Online)...");
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            capturedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
            byte[] imageBytes = baos.toByteArray();

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
                        displayPredictionResult(result, "Online");
                    } else {
                        // If online fails, try offline
                        if (loadingDialog != null) {
                            loadingDialog.setMessage("Online failed, trying offline...");
                        }
                        performOfflinePrediction();
                    }
                }

                @Override
                public void onFailure(Call<PredictResponse> call, Throwable t) {
                    // If online fails, try offline
                    if (loadingDialog != null) {
                        loadingDialog.setMessage("Online failed, trying offline...");
                    }
                    performOfflinePrediction();
                }
            });

        } catch (Exception e) {
            if (loadingDialog != null) loadingDialog.dismiss();
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Offline prediction method
    private void performOfflinePrediction() {
        if (!isFinishing()) {
            loadingDialog.show("Loading result (Offline)...");
        }

        // Run offline prediction in background thread
        new Thread(() -> {
            PredictResponse result = offlineModelManager.predictOffline(capturedBitmap);

            runOnUiThread(() -> {
                if (loadingDialog != null) loadingDialog.dismiss();

                if (result != null && "success".equals(result.status)) {
                    displayPredictionResult(result, "Offline");
                } else {
                    Toast.makeText(this, "Offline prediction failed: " +
                            (result != null ? result.message : "Unknown error"), Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    // Updated display method to show prediction source
    private void displayPredictionResult(PredictResponse result, String source) {
        try {
            diseaseName = result.predicted_disease;
            scientificName = result.disease_info.scientific_name;
            description = result.disease_info.description;
            causes = result.disease_info.cause;

            root.tvDiseaseName.setText(diseaseName);
            root.tvSciName.setText(scientificName);

            // Show prediction source
            String sourceText = "Prediction Source: " + source;
            Toast.makeText(this, sourceText, Toast.LENGTH_SHORT).show();

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
        if (offlineModelManager != null) {
            offlineModelManager.close();
        }
        super.onDestroy();
    }
}

// NetworkUtils.java
class NetworkUtils {
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    public static boolean isInternetAvailable() {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 google.com");
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

// OfflineModelManager.java
class OfflineModelManager {
    private Interpreter tfliteInterpreter;
    private Context context;
    private boolean isModelLoaded = false;

    // Dynamic disease data from Firebase
    private List<String> diseaseNames = new ArrayList<>();
    private Map<String, PredictResponse.DiseaseInfo> diseaseMetadata = new HashMap<>();
    private boolean isDataLoaded = false;

    public OfflineModelManager(Context context) {
        this.context = context;
        loadOfflineModel();
        loadDiseaseDataFromFirebase();
    }

    private void loadOfflineModel() {
        try {
            // Try to load from local storage first
            File localModelFile = new File(context.getFilesDir(), "stage2_disease_classifier.tflite");

            if (localModelFile.exists()) {
                // Load from local storage
                FileInputStream inputStream = new FileInputStream(localModelFile);
                FileChannel fileChannel = inputStream.getChannel();
                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, localModelFile.length());

                tfliteInterpreter = new Interpreter(buffer);
                isModelLoaded = true;

                Log.d("OfflineModel", "TFLite model loaded from local storage");
            } else {
                // Download from Firebase Storage
                downloadTFLiteModelFromFirebase();
            }

        } catch (Exception e) {
            Log.e("OfflineModel", "Failed to load TFLite model: " + e.getMessage());
            isModelLoaded = false;
        }
    }

    // Download TFLite model from Firebase Storage
    private void downloadTFLiteModelFromFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference modelRef = storage.getReference().child("models/stage2_disease_classifier.tflite");

        File localFile = new File(context.getFilesDir(), "stage2_disease_classifier.tflite");

        modelRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("OfflineModel", "TFLite model downloaded successfully");
                    // Retry loading after download
                    loadOfflineModel();
                })
                .addOnFailureListener(exception -> {
                    Log.e("OfflineModel", "Failed to download TFLite model: " + exception.getMessage());
                    // Fallback to assets
                    loadFromAssets();
                });
    }

    // Fallback to assets if download fails
    private void loadFromAssets() {
        try {
            android.content.res.AssetFileDescriptor fileDescriptor = context.getAssets().openFd("rice_disease_model.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

            tfliteInterpreter = new Interpreter(buffer);
            isModelLoaded = true;

            Log.d("OfflineModel", "TFLite model loaded from assets");
        } catch (Exception e) {
            Log.e("OfflineModel", "Failed to load TFLite model from assets: " + e.getMessage());
            isModelLoaded = false;
        }
    }

    // Load disease data from Firebase
    private void loadDiseaseDataFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("rice_local_diseases")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    diseaseNames.clear();
                    diseaseMetadata.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String diseaseName = document.getString("name");
                        if (diseaseName != null && !diseaseName.isEmpty()) {
                            diseaseNames.add(diseaseName);

                            // Store metadata
                            PredictResponse.DiseaseInfo info = new PredictResponse.DiseaseInfo();
                            info.scientific_name = document.getString("scientificName");
                            info.description = document.getString("description");
                            info.symptoms = document.getString("symptoms");
                            info.cause = document.getString("cause");
                            info.treatments = document.getString("treatments");

                            diseaseMetadata.put(diseaseName, info);
                        }
                    }

                    isDataLoaded = true;
                    Log.d("OfflineModel", "Loaded " + diseaseNames.size() + " diseases from Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("OfflineModel", "Failed to load disease data: " + e.getMessage());
                    // Fallback to basic data
                    loadFallbackData();
                });
    }

    // Fallback data if Firebase fails
    private void loadFallbackData() {
        diseaseNames.clear();
        diseaseNames.add("Unknown Disease");

        PredictResponse.DiseaseInfo fallbackInfo = new PredictResponse.DiseaseInfo();
        fallbackInfo.scientific_name = "Unknown";
        fallbackInfo.description = "Disease information not available offline";
        fallbackInfo.symptoms = "Symptoms not available";
        fallbackInfo.cause = "Cause not available";
        fallbackInfo.treatments = "Treatment not available";

        diseaseMetadata.put("Unknown Disease", fallbackInfo);
        isDataLoaded = true;
    }

    public PredictResponse predictOffline(Bitmap bitmap) {
        if (!isModelLoaded) {
            return createErrorResponse("Offline model not loaded");
        }

        if (!isDataLoaded) {
            return createErrorResponse("Disease data not loaded yet");
        }

        try {
            // Preprocess image
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            ByteBuffer inputBuffer = preprocessImage(resizedBitmap);

            // Run inference
            float[][] output = new float[1][getNumClasses()];
            tfliteInterpreter.run(inputBuffer, output);

            // Process results
            return processOfflineResults(output[0]);

        } catch (Exception e) {
            Log.e("OfflineModel", "Offline prediction failed: " + e.getMessage());
            return createErrorResponse("Offline prediction failed");
        }
    }

    private ByteBuffer preprocessImage(Bitmap bitmap) {
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[224 * 224];
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224);

        for (int pixel : pixels) {
            // Convert ARGB to RGB and normalize
            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        return inputBuffer;
    }

    private PredictResponse processOfflineResults(float[] predictions) {
        // Find highest confidence
        int maxIndex = 0;
        float maxConfidence = 0;
        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] > maxConfidence) {
                maxConfidence = predictions[i];
                maxIndex = i;
            }
        }

        // Get disease name from Firebase data
        String diseaseName;
        if (maxIndex < diseaseNames.size()) {
            diseaseName = diseaseNames.get(maxIndex);
        } else {
            diseaseName = "Unknown Disease";
        }

        // Create response similar to API
        PredictResponse response = new PredictResponse();
        response.predicted_disease = diseaseName;
        response.confidence = maxConfidence;
        response.status = "success";

        // Get disease info from Firebase data
        PredictResponse.DiseaseInfo diseaseInfo = diseaseMetadata.get(diseaseName);
        if (diseaseInfo == null) {
            diseaseInfo = diseaseMetadata.get("Unknown Disease");
        }

        response.disease_info = diseaseInfo;

        return response;
    }

    private int getNumClasses() {
        return diseaseNames.size(); // Dynamic based on Firebase data
    }

    private PredictResponse createErrorResponse(String message) {
        PredictResponse response = new PredictResponse();
        response.status = "error";
        response.message = message;
        return response;
    }

    public boolean isModelAvailable() {
        return isModelLoaded && isDataLoaded;
    }

    public boolean isDataReady() {
        return isDataLoaded;
    }

    public void close() {
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }
}