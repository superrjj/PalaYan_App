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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Stage2ModelManager {
    private Interpreter tfliteInterpreter;
    private Context context;
    private boolean isModelLoaded = false;
    private boolean isDataLoaded = false;

    // Configuration constants
    private static final float CONFIDENCE_THRESHOLD = 0.6f;
    private static final int INPUT_SIZE = 224;
    private static final int CHANNELS = 3;

    // ImageNet normalization constants for EfficientNet
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    // Disease data from Firebase Firestore
    private List<String> diseaseNames = new ArrayList<>();
    private Map<String, DiseaseInfo> diseaseMetadata = new HashMap<>();
    private int modelOutputSize = 2; //Change the comment
    private List<String> stage2Labels = new ArrayList<>();

    public Stage2ModelManager(Context context) {
        this.context = context;
        loadStage2Model();
        loadDiseaseDataFromFirebase();
        loadStage2Labels();
    }

    private void loadStage2Labels() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference labelsRef = storage.getReference().child("models/stage2_labels.txt");

        File localFile = new File(context.getFilesDir(), "stage2_labels.txt");
        labelsRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    try {
                        // Android-compatible file reading
                        List<String> labels = new ArrayList<>();
                        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(localFile));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            labels.add(line.trim());
                        }
                        reader.close();

                        stage2Labels.clear();
                        stage2Labels.addAll(labels);
                        Log.d("Stage2Model", "Loaded labels: " + stage2Labels);
                    } catch (Exception e) {
                        Log.e("Stage2Model", "Failed to read labels: " + e.getMessage());
                        // Fallback
                        stage2Labels.add("Healthy");
                        stage2Labels.add("Bacterial Leaf Blast");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Stage2Model", "Failed to download labels: " + e.getMessage());
                    // Fallback
                    stage2Labels.add("Healthy");
                    stage2Labels.add("Bacterial Leaf Blast");
                });
    }

    private void loadStage2Model() {
        try {
            File localModelFile = new File(context.getFilesDir(), "stage2_rice_disease_classifier.tflite");
            Log.d("Stage2Model", "Checking for local model file: " + localModelFile.getAbsolutePath());
            Log.d("Stage2Model", "File exists: " + localModelFile.exists());

            if (localModelFile.exists()) {
                loadModelFromFile(localModelFile);
            } else {
                Log.d("Stage2Model", "Local model file not found, downloading from Firebase...");
                downloadStage2ModelFromFirebase();
            }

        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load Stage 2 model: " + e.getMessage());
            isModelLoaded = false;
        }
    }

    private void loadModelFromFile(File modelFile) {
        try {
            FileInputStream inputStream = new FileInputStream(modelFile);
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length());

            tfliteInterpreter = new Interpreter(buffer);
            isModelLoaded = true;

            // Detect model output size dynamically
            try {
                int[] outputShape = tfliteInterpreter.getOutputTensor(0).shape();
                modelOutputSize = outputShape[1];
                Log.d("Stage2Model", "Detected model output size: " + modelOutputSize);
            } catch (Exception e) {
                Log.w("Stage2Model", "Could not detect output size, using default: " + modelOutputSize);
            }

            // Validate model
            if (validateModel()) {
                Log.d("Stage2Model", "Disease classification model loaded and validated successfully");
            } else {
                Log.e("Stage2Model", "Model validation failed");
                isModelLoaded = false;
            }

        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load model from file: " + e.getMessage());
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
                    loadModelFromFile(localFile);
                })
                .addOnFailureListener(exception -> {
                    Log.e("Stage2Model", "Failed to download Stage 2 model: " + exception.getMessage());
                    isModelLoaded = false;
                });
    }

    private boolean validateModel() {
        try {
            // Test with dummy input
            ByteBuffer testInput = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * CHANNELS);
            testInput.order(ByteOrder.nativeOrder());

            // Fill with random normalized data
            for (int i = 0; i < INPUT_SIZE * INPUT_SIZE * CHANNELS; i++) {
                testInput.putFloat((float) (Math.random() * 2 - 1)); // [-1, 1] range
            }

            float[][] testOutput = new float[1][modelOutputSize];
            tfliteInterpreter.run(testInput, testOutput);

            // Check if output is valid (sum should be close to 1.0 for softmax)
            float sum = 0;
            for (float value : testOutput[0]) {
                sum += value;
            }
            boolean isValid = Math.abs(sum - 1.0f) < 0.1f;

            Log.d("Stage2Model", "Model validation - Output sum: " + sum + ", Valid: " + isValid);
            return isValid;

        } catch (Exception e) {
            Log.e("Stage2Model", "Model validation failed: " + e.getMessage());
            return false;
        }
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

        // Healthy
        DiseaseInfo healthyInfo = new DiseaseInfo();
        healthyInfo.scientificName = "Oryza sativa";
        healthyInfo.description = "Malusog ang dahon ng palay";
        healthyInfo.symptoms = "Walang sakit ang palay";
        healthyInfo.cause = "Walang sakit ang palay";
        healthyInfo.treatments = "Walang sakit ang palay";

        // Bacterial Leaf Blast
        DiseaseInfo blbInfo = new DiseaseInfo();
        blbInfo.scientificName = "Xanthomonas oryzae pv. oryzae";
        blbInfo.description = "Bacterial Leaf Blast is a serious bacterial disease of rice.";
        blbInfo.symptoms = "Water-soaked lesions, yellowing leaves, wilting";
        blbInfo.cause = "Bacterial pathogen Xanthomonas oryzae pv. oryzae";
        blbInfo.treatments = "Apply copper-based fungicides, improve drainage, use resistant varieties";

        diseaseMetadata.put("Healthy", healthyInfo);
        diseaseMetadata.put("Bacterial Leaf Blast", blbInfo);

        isDataLoaded = true;
        Log.d("Stage2Model", "Loaded fallback data with " + diseaseNames.size() + " diseases");
    }

    public DiseaseResult predictDisease(Bitmap bitmap) {
        Log.d("Stage2Model", "=== STARTING DISEASE ANALYSIS ===");

        if (!isModelLoaded) {
            return new DiseaseResult(false, "Disease classification model not loaded", null);
        }

        if (!isDataLoaded) {
            return new DiseaseResult(false, "Disease data not loaded yet", null);
        }

        try {
            // Preprocess image with correct normalization
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
            ByteBuffer inputBuffer = preprocessImageCorrectly(resizedBitmap);

            // Run inference
            float[][] output = new float[1][modelOutputSize];
            long startTime = System.currentTimeMillis();
            tfliteInterpreter.run(inputBuffer, output);
            long inferenceTime = System.currentTimeMillis() - startTime;

            Log.d("Stage2Model", "Disease inference completed in: " + inferenceTime + "ms");

            // Process results
            return processDiseaseResults(output[0]);

        } catch (Exception e) {
            Log.e("Stage2Model", "Disease prediction failed: " + e.getMessage());
            e.printStackTrace();
            return new DiseaseResult(false, "Disease prediction failed: " + e.getMessage(), null);
        }
    }

    private ByteBuffer preprocessImageCorrectly(Bitmap bitmap) {
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * CHANNELS);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int pixel : pixels) {
            float r = ((pixel >> 16) & 0xFF) / 255.0f;
            float g = ((pixel >> 8) & 0xFF) / 255.0f;
            float b = (pixel & 0xFF) / 255.0f;

            // CORRECT: ImageNet normalization for EfficientNet
            r = (r - MEAN[0]) / STD[0];
            g = (g - MEAN[1]) / STD[1];
            b = (b - MEAN[2]) / STD[2];

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        return inputBuffer;
    }

    private DiseaseResult processDiseaseResults(float[] predictions) {
        Log.d("Stage2Model", "Raw predictions: " + java.util.Arrays.toString(predictions));
        Log.d("Stage2Model", "Model output size: " + modelOutputSize);

        // Find top prediction
        int maxIndex = 0;
        float maxConfidence = 0;
        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] > maxConfidence) {
                maxConfidence = predictions[i];
                maxIndex = i;
            }
        }

        Log.d("Stage2Model", "Top prediction - Index: " + maxIndex + ", Confidence: " + maxConfidence);

        // Map index to disease name
        String diseaseName = mapIndexToDiseaseName(maxIndex);

        // Apply confidence threshold
        if (maxConfidence < CONFIDENCE_THRESHOLD) {
            diseaseName = "Uncertain - Low Confidence (" + String.format("%.1f", maxConfidence * 100) + "%)";
            Log.w("Stage2Model", "Low confidence prediction: " + maxConfidence);
        }

        // Get disease info
        DiseaseInfo diseaseInfo = diseaseMetadata.get(diseaseName);
        if (diseaseInfo == null) {
            Log.w("Stage2Model", "No disease info found for: " + diseaseName);
            diseaseInfo = createDefaultDiseaseInfo(diseaseName);
        }

        Log.d("Stage2Model", "Final predicted disease: " + diseaseName + " (confidence: " + maxConfidence + ")");

        return new DiseaseResult(true, null, diseaseName, maxConfidence, diseaseInfo);
    }

    private String mapIndexToDiseaseName(int index) {
        // Map based on your 2-class model
        if (index == 0) {
            return "Healthy";
        } else if (index == 1) {
            return "Bacterial Leaf Blast";
        } else {
            return "Unknown Disease (Index: " + index + ")";
        }
    }

    private DiseaseInfo createDefaultDiseaseInfo(String diseaseName) {
        DiseaseInfo info = new DiseaseInfo();
        info.scientificName = "Unknown";
        info.description = "No description available for " + diseaseName;
        info.symptoms = "No symptoms information available";
        info.cause = "No cause information available";
        info.treatments = "No treatment information available";
        return info;
    }

    public boolean isModelReady() {
        return isModelLoaded && isDataLoaded;
    }

    public void close() {
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }

    // Helper classes
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

    public static class DiseaseInfo {
        public String scientificName;
        public String description;
        public String symptoms;
        public String cause;
        public String treatments;
    }
}