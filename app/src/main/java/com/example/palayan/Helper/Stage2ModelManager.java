package com.example.palayan.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.tensorflow.lite.Interpreter;

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

public class Stage2ModelManager {
    private Interpreter tfliteInterpreter;
    private Context context;
    private boolean isModelLoaded = false;
    private boolean isDataLoaded = false;

    // Disease data from Firebase Firestore
    private List<String> diseaseNames = new ArrayList<>();
    private Map<String, DiseaseInfo> diseaseMetadata = new HashMap<>();

    // Model output size
    private int modelOutputSize = 2; // Healthy, Bacterial Leaf Blast

    public Stage2ModelManager(Context context) {
        this.context = context;
        loadStage2Model();
        loadDiseaseDataFromFirebase();
    }

    private void loadStage2Model() {
        try {
            // Try to load from local storage first
            File localModelFile = new File(context.getFilesDir(), "stage2_rice_disease_classifier.tflite");
            Log.d("Stage2Model", "Checking for local model file: " + localModelFile.getAbsolutePath());
            Log.d("Stage2Model", "File exists: " + localModelFile.exists());

            if (localModelFile.exists()) {
                // Load from local storage
                FileInputStream inputStream = new FileInputStream(localModelFile);
                FileChannel fileChannel = inputStream.getChannel();
                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, localModelFile.length());

                tfliteInterpreter = new Interpreter(buffer);
                isModelLoaded = true;

                // Get actual model output size
                try {
                    int[] outputShape = tfliteInterpreter.getOutputTensor(0).shape();
                    modelOutputSize = outputShape[1];
                    Log.d("Stage2Model", "Model output shape: " + java.util.Arrays.toString(outputShape));
                } catch (Exception e) {
                    Log.w("Stage2Model", "Could not get model output shape, using default: " + modelOutputSize);
                }

                Log.d("Stage2Model", "Disease classification model loaded from local storage");
                Log.d("Stage2Model", "Model output size: " + modelOutputSize);
            } else {
                Log.d("Stage2Model", "Local model file not found, downloading from Firebase...");
                // Download from Firebase Storage
                downloadStage2ModelFromFirebase();
            }

        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load Stage 2 model: " + e.getMessage());
            isModelLoaded = false;
        }
    }

    private void downloadStage2ModelFromFirebase() {
        Log.d("Stage2Model", "Starting to download Stage 2 model from Firebase Storage...");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference modelRef = storage.getReference().child("models/stage2_rice_disease_classifier.tflite");

        File localFile = new File(context.getFilesDir(), "stage2_rice_disease_classifier.tflite");
        Log.d("Stage2Model", "Local file path: " + localFile.getAbsolutePath());

        modelRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Stage2Model", "Stage 2 model downloaded successfully");
                    Log.d("Stage2Model", "Downloaded file size: " + localFile.length() + " bytes");
                    // Retry loading after download
                    loadStage2Model();
                })
                .addOnFailureListener(exception -> {
                    Log.e("Stage2Model", "Failed to download Stage 2 model: " + exception.getMessage());
                    isModelLoaded = false;
                });
    }

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
                            DiseaseInfo info = new DiseaseInfo();
                            info.scientificName = document.getString("scientificName");
                            info.description = document.getString("description") != null ?
                                    document.getString("description") : "No description available";
                            info.symptoms = document.getString("symptoms") != null ?
                                    document.getString("symptoms") : "No symptoms information available";
                            info.cause = document.getString("cause") != null ?
                                    document.getString("cause") : "No cause information available";
                            info.treatments = document.getString("treatments") != null ?
                                    document.getString("treatments") : "No treatment information available";

                            diseaseMetadata.put(diseaseName, info);

                            Log.d("Stage2Model", "Loaded disease: " + diseaseName);
                        }
                    }

                    isDataLoaded = true;
                    Log.d("Stage2Model", "Loaded " + diseaseNames.size() + " diseases from Firebase");
                })
                .addOnFailureListener(e -> {
                    Log.e("Stage2Model", "Failed to load disease data: " + e.getMessage());
                    loadFallbackData();
                });
    }

    private void loadFallbackData() {
        diseaseNames.clear();
        diseaseNames.add("Healthy");
        diseaseNames.add("Bacterial Leaf Blast");

        DiseaseInfo healthyInfo = new DiseaseInfo();
        healthyInfo.scientificName = "Oryza sativa";
        healthyInfo.description = "Malusog ang dahon ng palay";
        healthyInfo.symptoms = "Walang sakit ang palay";
        healthyInfo.cause = "Walang sakit ang palay";
        healthyInfo.treatments = "Walang sakit ang palay";

        DiseaseInfo blbInfo = new DiseaseInfo();
        blbInfo.scientificName = "Xanthomonas oryzae pv. oryzae";
        blbInfo.description = "Bacterial Leaf Blast is a serious bacterial disease of rice.";
        blbInfo.symptoms = "Water-soaked lesions, yellowing leaves, wilting";
        blbInfo.cause = "Bacterial pathogen Xanthomonas oryzae pv. oryzae";
        blbInfo.treatments = "Apply copper-based fungicides, improve drainage, use resistant varieties";

        diseaseMetadata.put("Healthy", healthyInfo);
        diseaseMetadata.put("Bacterial Leaf Blast", blbInfo);
        isDataLoaded = true;
    }

    public DiseaseResult predictDisease(Bitmap bitmap) {
        if (!isModelLoaded) {
            return new DiseaseResult(false, "Disease classification model not loaded", null);
        }

        if (!isDataLoaded) {
            return new DiseaseResult(false, "Disease data not loaded yet", null);
        }

        try {
            // Preprocess image
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            ByteBuffer inputBuffer = preprocessImage(resizedBitmap);

            // Run inference
            float[][] output = new float[1][modelOutputSize];
            tfliteInterpreter.run(inputBuffer, output);

            // Process results
            return processDiseaseResults(output[0]);

        } catch (Exception e) {
            Log.e("Stage2Model", "Disease prediction failed: " + e.getMessage());
            return new DiseaseResult(false, "Disease prediction failed", null);
        }
    }

    private ByteBuffer preprocessImage(Bitmap bitmap) {
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[224 * 224];
        bitmap.getPixels(pixels, 0, 224, 0, 0, 224, 224);

        for (int pixel : pixels) {
            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;

            // Normalize to [-1, 1] range
            r = (r - 0.5f) / 0.5f;
            g = (g - 0.5f) / 0.5f;
            b = (b - 0.5f) / 0.5f;

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        return inputBuffer;
    }

    private DiseaseResult processDiseaseResults(float[] predictions) {
        Log.d("Stage2Model", "Raw predictions: " + java.util.Arrays.toString(predictions));
        Log.d("Stage2Model", "Disease names: " + diseaseNames.toString());

        // Find highest confidence
        int maxIndex = 0;
        float maxConfidence = 0;
        for (int i = 0; i < predictions.length; i++) {
            Log.d("Stage2Model", "Index " + i + ": " + predictions[i]);
            if (predictions[i] > maxConfidence) {
                maxConfidence = predictions[i];
                maxIndex = i;
            }
        }

        Log.d("Stage2Model", "Max index: " + maxIndex + ", Max confidence: " + maxConfidence);

        // Map index to disease name
        String diseaseName;
        if (maxIndex == 0) {
            diseaseName = "Healthy";
        } else if (maxIndex == 1) {
            diseaseName = "Bacterial Leaf Blast";
        } else {
            diseaseName = "Unknown Disease";
        }

        // Get disease info
        DiseaseInfo diseaseInfo = diseaseMetadata.get(diseaseName);
        if (diseaseInfo == null) {
            Log.w("Stage2Model", "No disease info found for: " + diseaseName);
            diseaseInfo = new DiseaseInfo();
            diseaseInfo.scientificName = "Unknown";
            diseaseInfo.description = "No description available";
            diseaseInfo.symptoms = "No symptoms information available";
            diseaseInfo.cause = "No cause information available";
            diseaseInfo.treatments = "No treatment information available";
        }

        Log.d("Stage2Model", "Final predicted disease: " + diseaseName + " (index: " + maxIndex + ", confidence: " + maxConfidence + ")");

        return new DiseaseResult(true, null, diseaseName, maxConfidence, diseaseInfo);
    }

    public boolean isModelReady() {
        return isModelLoaded && isDataLoaded;
    }

    public void close() {
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }

    // Disease result class
    public static class DiseaseResult {
        public boolean isSuccess;
        public String errorMessage;
        public String diseaseName;
        public float confidence;
        public DiseaseInfo diseaseInfo;

        public DiseaseResult(boolean isSuccess, String errorMessage, String diseaseName, float confidence, DiseaseInfo diseaseInfo) {
            this.isSuccess = isSuccess;
            this.errorMessage = errorMessage;
            this.diseaseName = diseaseName;
            this.confidence = confidence;
            this.diseaseInfo = diseaseInfo;
        }

        public DiseaseResult(boolean isSuccess, String errorMessage, DiseaseInfo diseaseInfo) {
            this.isSuccess = isSuccess;
            this.errorMessage = errorMessage;
            this.diseaseInfo = diseaseInfo;
        }
    }

    // Disease info class
    public static class DiseaseInfo {
        public String scientificName;
        public String description;
        public String symptoms;
        public String cause;
        public String treatments;
    }
}