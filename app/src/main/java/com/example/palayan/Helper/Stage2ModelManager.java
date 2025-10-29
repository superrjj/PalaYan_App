package com.example.palayan.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Stage2ModelManager {
    
    // Disease result class
    public static class DiseaseResult {
        public boolean isSuccess;
        public String diseaseName;
        public float confidence;
        public String errorMessage;
        public DiseaseInfo diseaseInfo;
        
        public DiseaseResult(boolean isSuccess, String diseaseName, float confidence, String errorMessage, DiseaseInfo diseaseInfo) {
            this.isSuccess = isSuccess;
            this.diseaseName = diseaseName;
            this.confidence = confidence;
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
        
        public DiseaseInfo(String scientificName, String description, String symptoms, String cause, String treatments) {
            this.scientificName = scientificName;
            this.description = description;
            this.symptoms = symptoms;
            this.cause = cause;
            this.treatments = treatments;
        }
    }
    private Interpreter tfliteInterpreter;
    private Context context;
    private boolean isModelLoaded = false;

    // Configuration constants
    private static final float CONFIDENCE_THRESHOLD = 0.5f;
    private static final int INPUT_SIZE = 224;
    private static final int CHANNELS = 3;
    private static final int modelOutputSize = 3; // 3 diseases: Bacterial Leaf Blight, Brown Spot, Healthy
    private List<String> stage2Labels = new ArrayList<>();

    // ImageNet normalization constants for EfficientNet
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    public Stage2ModelManager(Context context) {
        this.context = context;
        loadStage2Model();
        loadStage2Labels();
    }

    private void loadStage2Model() {
        try {
            Log.d("Stage2Model", "Loading Stage 2 model...");
            
            // Try to load from assets first
            if (loadModelFromAssets()) {
                Log.d("Stage2Model", "Stage 2 model loaded from assets successfully");
                isModelLoaded = true;
                return;
            }
            
            // If assets loading fails, try to load from file
            if (loadModelFromFile()) {
                Log.d("Stage2Model", "Stage 2 model loaded from file successfully");
                isModelLoaded = true;
                return;
            }
            
            Log.e("Stage2Model", "Failed to load Stage 2 model from both assets and file");
            isModelLoaded = false;
            
        } catch (Exception e) {
            Log.e("Stage2Model", "Error loading Stage 2 model: " + e.getMessage());
            e.printStackTrace();
            isModelLoaded = false;
        }
    }

    private boolean loadModelFromAssets() {
        // Try models/ path first, then root assets as fallback
        final String[] candidatePaths = new String[] {
                "models/stage2_rice_disease_classifier.tflite",
                "stage2_rice_disease_classifier.tflite"
        };
        for (String assetPath : candidatePaths) {
            try {
                InputStream inputStream = context.getAssets().open(assetPath);
                byte[] modelBytes = new byte[inputStream.available()];
                inputStream.read(modelBytes);
                inputStream.close();

                // Convert byte array to ByteBuffer
                ByteBuffer modelBuffer = ByteBuffer.allocateDirect(modelBytes.length);
                modelBuffer.put(modelBytes);
                modelBuffer.rewind();

                Interpreter.Options options = new Interpreter.Options();
                tfliteInterpreter = new Interpreter(modelBuffer, options);
                Log.d("Stage2Model", "Model loaded from assets (" + assetPath + "), size: " + modelBytes.length + " bytes");
                return true;
            } catch (Exception e) {
                Log.w("Stage2Model", "Could not load model at assets/" + assetPath + ": " + e.getMessage());
            }
        }
        Log.e("Stage2Model", "Failed to load model from any asset path");
        return false;
    }

    private boolean loadModelFromFile() {
        try {
            File modelFile = new File(context.getFilesDir(), "stage2_rice_disease_classifier.tflite");
            if (!modelFile.exists()) {
                Log.e("Stage2Model", "Model file does not exist: " + modelFile.getAbsolutePath());
                return false;
            }
            
            FileInputStream inputStream = new FileInputStream(modelFile);
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
            
            Interpreter.Options options = new Interpreter.Options();
            tfliteInterpreter = new Interpreter(buffer, options);
            Log.d("Stage2Model", "Model loaded from file, size: " + modelFile.length() + " bytes");
            return true;
        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load model from file: " + e.getMessage());
            return false;
        }
    }

    private void loadStage2Labels() {
        stage2Labels.clear();
        final String[] candidatePaths = new String[] {
                "models/stage2_labels.txt",
                "stage2_labels.txt"
        };

        for (String assetPath : candidatePaths) {
            try {
                InputStream inputStream = context.getAssets().open(assetPath);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                inputStream.close();

                String labelsContent = new String(buffer);
                String[] lines = labelsContent.split("\n");

                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        stage2Labels.add(line.trim());
                    }
                }

                Log.d("Stage2Model", "Loaded " + stage2Labels.size() + " labels from assets/" + assetPath + ": " + stage2Labels);
                if (!stage2Labels.isEmpty()) return;
            } catch (Exception e) {
                Log.w("Stage2Model", "Could not load labels at assets/" + assetPath + ": " + e.getMessage());
            }
        }

        if (stage2Labels.isEmpty()) {
            Log.e("Stage2Model", "Failed to load labels from assets. Using fallback labels.");
            // Fallback labels (MUST match model output order)
            stage2Labels.add("Bacterial Leaf Blight");
            stage2Labels.add("Brown Spot");
            stage2Labels.add("Healthy");
        }
    }

    public String detectDisease(String imagePath) {
        Log.d("Stage2Model", "=== STARTING DISEASE ANALYSIS ===");
        Log.d("Stage2Model", "Image path: " + imagePath);

        if (!isModelLoaded) {
            Log.e("Stage2Model", "Model not loaded - returning fallback");
            return "Model not loaded";
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                Log.e("Stage2Model", "Failed to load image - returning fallback");
                return "Failed to load image";
            }

            Log.d("Stage2Model", "Original image size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // Center-crop to reduce background, then resize
            Bitmap cropped = centerCropToSquare(bitmap);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true);
            ByteBuffer inputBuffer = preprocessImageCorrectly(resizedBitmap);

            // Run ML inference
            float[][] output = new float[1][modelOutputSize];
            long startTime = System.currentTimeMillis();
            tfliteInterpreter.run(inputBuffer, output);
            long inferenceTime = System.currentTimeMillis() - startTime;

            Log.d("Stage2Model", "ML inference completed in: " + inferenceTime + "ms");

            // Process results
            String result = processDiseaseResults(output[0]);
            
            Log.d("Stage2Model", "=== FINAL DISEASE RESULT ===");
            Log.d("Stage2Model", "Detected disease: " + result);
            Log.d("Stage2Model", "🎯 RESULT: " + result);

            return result;

        } catch (Exception e) {
            Log.e("Stage2Model", "Analysis failed: " + e.getMessage());
            e.printStackTrace();
            return "Analysis failed";
        }
    }

    private String processDiseaseResults(float[] predictions) {
        // Apply bias correction to prevent Bacterial Leaf Blight dominance
        float[] correctedPredictions = applyBiasCorrection(predictions);
        
        // Find the index with highest confidence
        int maxIndex = 0;
        float maxConfidence = correctedPredictions[0];
        
        for (int i = 1; i < correctedPredictions.length; i++) {
            if (correctedPredictions[i] > maxConfidence) {
                maxConfidence = correctedPredictions[i];
                maxIndex = i;
            }
        }
        
        // Map index to disease name
        String diseaseName = mapIndexToDiseaseName(maxIndex);
        
        Log.d("Stage2Model", "Raw predictions: [" + predictions[0] + ", " + predictions[1] + ", " + predictions[2] + "]");
        Log.d("Stage2Model", "Corrected predictions: [" + correctedPredictions[0] + ", " + correctedPredictions[1] + ", " + correctedPredictions[2] + "]");
        Log.d("Stage2Model", "Max index: " + maxIndex + ", Confidence: " + maxConfidence);
        
        return diseaseName;
    }

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
        switch (index) {
            case 0:
                return "Bacterial Leaf Blight";
            case 1:
                return "Brown Spot";
            case 2:
                return "Healthy";
            default:
                return "Unknown";
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

    private Bitmap centerCropToSquare(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w == h) return src;
        int size = Math.min(w, h);
        int x = (w - size) / 2;
        int y = (h - size) / 2;
        try {
            return Bitmap.createBitmap(src, x, y, size, size);
        } catch (Exception e) {
            return src;
        }
    }

    private Bitmap rotateBitmap(Bitmap src, float degrees) {
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.postRotate(degrees);
        try {
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), m, true);
        } catch (Exception e) {
            return src;
        }
    }

    public DiseaseResult predictDisease(Bitmap bitmap) {
        Log.d("Stage2Model", "=== STARTING DISEASE PREDICTION ===");
        
        if (!isModelLoaded) {
            Log.e("Stage2Model", "Model not loaded");
            return new DiseaseResult(false, null, 0f, "Model not loaded", null);
        }

        try {
            Log.d("Stage2Model", "Original image size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // Preprocess image with Keras EfficientNet normalization
            Bitmap cropped = centerCropToSquare(bitmap);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(cropped, INPUT_SIZE, INPUT_SIZE, true);
            ByteBuffer inputBuffer = preprocessImageCorrectly(resizedBitmap);

            // Run ML inference
            float[][] output = new float[1][modelOutputSize];
            long startTime = System.currentTimeMillis();
            tfliteInterpreter.run(inputBuffer, output);
            long inferenceTime = System.currentTimeMillis() - startTime;

            Log.d("Stage2Model", "ML inference completed in: " + inferenceTime + "ms");

            // Process results
            float[] correctedPredictions = applyBiasCorrection(output[0]);
            
            // Find the index with highest confidence
            int maxIndex = 0;
            float maxConfidence = correctedPredictions[0];
            
            for (int i = 1; i < correctedPredictions.length; i++) {
                if (correctedPredictions[i] > maxConfidence) {
                    maxConfidence = correctedPredictions[i];
                    maxIndex = i;
                }
            }
            
            // Map index to disease name (prefer labels file if available)
            String diseaseName;
            if (stage2Labels != null && stage2Labels.size() >= modelOutputSize && maxIndex < stage2Labels.size()) {
                diseaseName = stage2Labels.get(maxIndex);
            } else {
                diseaseName = mapIndexToDiseaseName(maxIndex);
            }
            
            Log.d("Stage2Model", "Raw predictions: [" + output[0][0] + ", " + output[0][1] + ", " + output[0][2] + "]");
            Log.d("Stage2Model", "Corrected predictions: [" + correctedPredictions[0] + ", " + correctedPredictions[1] + ", " + correctedPredictions[2] + "]");
            Log.d("Stage2Model", "Max index: " + maxIndex + ", Confidence: " + maxConfidence);
            
            // Get disease info
            DiseaseInfo diseaseInfo = getDiseaseInfo(diseaseName);
            
            Log.d("Stage2Model", "=== FINAL DISEASE RESULT ===");
            Log.d("Stage2Model", "Detected disease: " + diseaseName + " (confidence: " + maxConfidence + ")");
            
            // If confidence is low, try a rotated pass (90°) for angle robustness
            if (maxConfidence < 0.5f) {
                Bitmap rotated = rotateBitmap(cropped, 90);
                Bitmap resizedRot = Bitmap.createScaledBitmap(rotated, INPUT_SIZE, INPUT_SIZE, true);
                ByteBuffer inputRot = preprocessImageCorrectly(resizedRot);
                float[][] outRot = new float[1][modelOutputSize];
                tfliteInterpreter.run(inputRot, outRot);
                float[] correctedRot = applyBiasCorrection(outRot[0]);
                int maxIdxRot = 0; float maxConfRot = correctedRot[0];
                for (int i = 1; i < correctedRot.length; i++) {
                    if (correctedRot[i] > maxConfRot) { maxConfRot = correctedRot[i]; maxIdxRot = i; }
                }
                if (maxConfRot > maxConfidence) {
                    maxConfidence = maxConfRot;
                    diseaseName = (stage2Labels != null && stage2Labels.size() > maxIdxRot) ? stage2Labels.get(maxIdxRot) : mapIndexToDiseaseName(maxIdxRot);
                    diseaseInfo = getDiseaseInfo(diseaseName);
                }
            }

            return new DiseaseResult(true, diseaseName, maxConfidence, null, diseaseInfo);

        } catch (Exception e) {
            Log.e("Stage2Model", "Prediction failed: " + e.getMessage());
            e.printStackTrace();
            return new DiseaseResult(false, null, 0f, "Prediction failed: " + e.getMessage(), null);
        }
    }
    
    private DiseaseInfo getDiseaseInfo(String diseaseName) {
        switch (diseaseName) {
            case "Bacterial Leaf Blight":
                return new DiseaseInfo(
                    "Xanthomonas oryzae pv. oryzae",
                    "Bacterial leaf blight is a serious disease of rice caused by the bacterium Xanthomonas oryzae pv. oryzae. It can cause significant yield losses in rice production.",
                    "Water-soaked lesions on leaf tips and margins, yellowing of leaves, wilting, and eventual death of affected plants.",
                    "Bacterial infection through wounds, contaminated water, or infected seeds. Favored by high humidity and warm temperatures.",
                    "Use resistant varieties, proper field sanitation, avoid overhead irrigation, apply copper-based fungicides, and practice crop rotation."
                );
            case "Brown Spot":
                return new DiseaseInfo(
                    "Cochliobolus miyabeanus",
                    "Brown spot is a fungal disease of rice caused by Cochliobolus miyabeanus. It affects rice leaves and can reduce grain quality and yield.",
                    "Small, circular to oval brown spots on leaves, spots may coalesce to form larger lesions, yellowing and premature leaf drop.",
                    "Fungal infection favored by high humidity, warm temperatures, and poor soil fertility. Spreads through spores.",
                    "Improve soil fertility, use resistant varieties, proper field drainage, apply fungicides, and remove infected plant debris."
                );
            case "Healthy":
                return new DiseaseInfo(
                    "Oryza sativa (Healthy)",
                    "Healthy rice plant showing no signs of disease. Proper growth and development with normal green coloration.",
                    "Normal green leaves, healthy growth pattern, no visible lesions or discoloration, proper plant structure.",
                    "No disease present. Maintained through proper cultivation practices and disease prevention.",
                    "Continue current practices: proper irrigation, balanced fertilization, regular monitoring, and preventive measures."
                );
            default:
                return new DiseaseInfo(
                    "Unknown",
                    "Disease identification not available.",
                    "Symptoms not documented.",
                    "Cause not identified.",
                    "Treatment recommendations not available."
                );
        }
    }

    public boolean isModelReady() {
        return isModelLoaded;
    }

    public void close() {
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }
}