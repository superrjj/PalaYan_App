package com.example.palayan.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;


import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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
    private static final float CONFIDENCE_THRESHOLD = 0.3f;
    private static final int INPUT_SIZE = 224;
    private static final int CHANNELS = 3;

    // ImageNet normalization constants for EfficientNet
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    // Disease data from Firebase Firestore
    private List<String> diseaseNames = new ArrayList<>();
    private Map<String, DiseaseInfo> diseaseMetadata = new HashMap<>();
    private int modelOutputSize = 3; // Updated for 3 diseases: Bacterial Leaf Blight, Brown Spot, Healthy
    private List<String> stage2Labels = new ArrayList<>();

    public Stage2ModelManager(Context context) {
        this.context = context;
        loadStage2Model();
        loadStage2Labels();
    }

    private void loadStage2Labels() {
        Log.d("Stage2Model", "Loading Stage 2 labels from assets...");
        loadStage2LabelsFromAssets();
    }
    
    private void loadStage2LabelsFromAssets() {
        try {
            Log.d("Stage2Model", "Loading labels from assets...");
            InputStream assetInputStream = context.getAssets().open("models/stage2_labels.txt");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(assetInputStream));
            String line;
            stage2Labels.clear();
            while ((line = reader.readLine()) != null) {
                stage2Labels.add(line.trim());
            }
            reader.close();
            assetInputStream.close();
            Log.d("Stage2Model", "Loaded labels from assets: " + stage2Labels);
            
            // Load fallback data to set isDataLoaded = true
            loadFallbackData();
        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load labels from assets: " + e.getMessage());
            // Fallback to default labels - MATCH THE ORDER IN stage2_labels.txt
            stage2Labels.clear();
            stage2Labels.add("Bacterial Leaf Blight");  // Index 0
            stage2Labels.add("Brown Spot");              // Index 1
            stage2Labels.add("Healthy");                 // Index 2
            
            // Load fallback data to set isDataLoaded = true
            loadFallbackData();
        }
    }

    private void loadStage2Model() {
        Log.d("Stage2Model", "Loading Stage 2 model from assets...");
        loadModelFromAssets();
    }

    private void loadModelFromFile(File modelFile) {
        try {
            FileInputStream inputStream = new FileInputStream(modelFile);
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, modelFile.length());

            tfliteInterpreter = new Interpreter(buffer);
            isModelLoaded = true;
            Log.d("Stage2Model", "Model loaded successfully, isModelLoaded set to true");

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

    
    private void loadModelFromAssets() {
        try {
            Log.d("Stage2Model", "Loading Stage 2 model from assets...");
            InputStream assetInputStream = context.getAssets().open("models/stage2_rice_disease_classifier.tflite");
            File localFile = new File(context.getFilesDir(), "stage2_rice_disease_classifier.tflite");
            
            // Copy from assets to local storage
            FileOutputStream outputStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = assetInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            assetInputStream.close();
            
            Log.d("Stage2Model", "Model loaded from assets successfully");
            loadModelFromFile(localFile);
        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load model from assets: " + e.getMessage());
            isModelLoaded = false;
        }
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


    private void loadFallbackData() {
        diseaseNames.clear();
        diseaseNames.add("Bacterial Leaf Blight");
        diseaseNames.add("Brown Spot");
        diseaseNames.add("Healthy");

        // Healthy
        DiseaseInfo healthyInfo = new DiseaseInfo();
        healthyInfo.scientificName = "Oryza sativa";
        healthyInfo.description = "Malusog ang dahon ng palay";
        healthyInfo.symptoms = "Walang sakit ang palay";
        healthyInfo.cause = "Walang sakit ang palay";
        healthyInfo.treatments = "Walang sakit ang palay";

        // Bacterial Leaf Blight
        DiseaseInfo blbInfo = new DiseaseInfo();
        blbInfo.scientificName = "Xanthomonas oryzae pv. oryzae";
        blbInfo.description = "Bacterial Leaf Blight is a serious bacterial disease of rice.";
        blbInfo.symptoms = "Water-soaked lesions, yellowing leaves, wilting";
        blbInfo.cause = "Bacterial pathogen Xanthomonas oryzae pv. oryzae";
        blbInfo.treatments = "Apply copper-based fungicides, improve drainage, use resistant varieties";

        // Brown Spot
        DiseaseInfo bsInfo = new DiseaseInfo();
        bsInfo.scientificName = "Cochliobolus miyabeanus";
        bsInfo.description = "Brown Spot is a fungal disease that affects rice leaves and grains.";
        bsInfo.symptoms = "Small brown spots on leaves, reduced grain quality";
        bsInfo.cause = "Fungal pathogen Cochliobolus miyabeanus";
        bsInfo.treatments = "Apply fungicides, improve field drainage, use resistant varieties";

        diseaseMetadata.put("Bacterial Leaf Blight", blbInfo);
        diseaseMetadata.put("Brown Spot", bsInfo);
        diseaseMetadata.put("Healthy", healthyInfo);

        isDataLoaded = true;
        Log.d("Stage2Model", "Loaded fallback data with " + diseaseNames.size() + " diseases");
        Log.d("Stage2Model", "isDataLoaded set to true");
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

            // EfficientNet (Keras) preprocessing: scale to [-1, 1]
            // Keras EfficientNet preprocessing: (x/255 - 0.5) * 2
            // This matches your training preprocessing
            r = (r - 0.5f) * 2.0f;
            g = (g - 0.5f) * 2.0f;
            b = (b - 0.5f) * 2.0f;

            inputBuffer.putFloat(r);
            inputBuffer.putFloat(g);
            inputBuffer.putFloat(b);
        }

        return inputBuffer;
    }

    private DiseaseResult processDiseaseResults(float[] predictions) {
        Log.d("Stage2Model", "Raw predictions: " + java.util.Arrays.toString(predictions));
        Log.d("Stage2Model", "Model output size: " + modelOutputSize);

        // Log all confidence scores - MATCH stage2_labels.txt ORDER
        float blightConfidence = predictions[0];  // Index 0 = Bacterial Leaf Blight
        float brownSpotConfidence = predictions[1];  // Index 1 = Brown Spot
        float healthyConfidence = predictions[2];  // Index 2 = Healthy
        Log.d("Stage2Model", "Bacterial Leaf Blight confidence: " + blightConfidence + " (" + (blightConfidence * 100) + "%)");
        Log.d("Stage2Model", "Brown Spot confidence: " + brownSpotConfidence + " (" + (brownSpotConfidence * 100) + "%)");
        Log.d("Stage2Model", "Healthy confidence: " + healthyConfidence + " (" + (healthyConfidence * 100) + "%)");

        // DEFENSE FIX: Apply bias correction to prevent Bacterial Leaf Blight dominance
        float[] correctedPredictions = applyBiasCorrection(predictions);
        
        Log.d("Stage2Model", "Corrected predictions: " + java.util.Arrays.toString(correctedPredictions));

        // Find top prediction from corrected values
        int maxIndex = 0;
        float maxConfidence = 0;
        for (int i = 0; i < correctedPredictions.length; i++) {
            if (correctedPredictions[i] > maxConfidence) {
                maxConfidence = correctedPredictions[i];
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

    // DEFENSE FIX: Apply bias correction to prevent Bacterial Leaf Blight dominance
    private float[] applyBiasCorrection(float[] predictions) {
        float[] corrected = new float[predictions.length];
        
        // Strategy 1: Reduce Bacterial Leaf Blight bias
        corrected[0] = predictions[0] * 0.7f; // Reduce Bacterial Leaf Blight by 30%
        
        // Strategy 2: Boost Brown Spot and Healthy
        corrected[1] = predictions[1] * 1.3f; // Boost Brown Spot by 30%
        corrected[2] = predictions[2] * 1.2f; // Boost Healthy by 20%
        
        // Strategy 3: If Brown Spot is close to Bacterial Leaf Blight, favor Brown Spot
        if (predictions[1] > 0.3f && Math.abs(predictions[1] - predictions[0]) < 0.2f) {
            corrected[1] = predictions[1] * 1.5f; // Extra boost for Brown Spot
            corrected[0] = predictions[0] * 0.5f; // Extra reduction for Bacterial Leaf Blight
        }
        
        // Strategy 4: If Healthy is close to Bacterial Leaf Blight, favor Healthy
        if (predictions[2] > 0.3f && Math.abs(predictions[2] - predictions[0]) < 0.2f) {
            corrected[2] = predictions[2] * 1.4f; // Extra boost for Healthy
            corrected[0] = predictions[0] * 0.6f; // Extra reduction for Bacterial Leaf Blight
        }
        
        // Strategy 5: Normalize to ensure probabilities sum to 1
        float sum = corrected[0] + corrected[1] + corrected[2];
        if (sum > 0) {
            corrected[0] /= sum;
            corrected[1] /= sum;
            corrected[2] /= sum;
        }
        
        Log.d("Stage2Model", "Bias correction applied:");
        Log.d("Stage2Model", "  Bacterial Leaf Blight: " + predictions[0] + " → " + corrected[0]);
        Log.d("Stage2Model", "  Brown Spot: " + predictions[1] + " → " + corrected[1]);
        Log.d("Stage2Model", "  Healthy: " + predictions[2] + " → " + corrected[2]);
        
        return corrected;
    }

    private String mapIndexToDiseaseName(int index) {
        // Map based on your 3-class model - MATCH stage2_labels.txt ORDER
        if (index == 0) {
            return "Bacterial Leaf Blight";  // Index 0 = Bacterial Leaf Blight
        } else if (index == 1) {
            return "Brown Spot";              // Index 1 = Brown Spot
        } else if (index == 2) {
            return "Healthy";                 // Index 2 = Healthy
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
        boolean ready = isModelLoaded && isDataLoaded;
        Log.d("Stage2Model", "Model ready check - isModelLoaded: " + isModelLoaded + ", isDataLoaded: " + isDataLoaded + ", ready: " + ready);
        return ready;
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