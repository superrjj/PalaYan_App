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
        try {
            InputStream inputStream = context.getAssets().open("stage2_rice_disease_classifier.tflite");
            byte[] modelBytes = new byte[inputStream.available()];
            inputStream.read(modelBytes);
            inputStream.close();
            
            tfliteInterpreter = new Interpreter(modelBytes);
            Log.d("Stage2Model", "Model loaded from assets, size: " + modelBytes.length + " bytes");
            return true;
        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load model from assets: " + e.getMessage());
            return false;
        }
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
            
            tfliteInterpreter = new Interpreter(buffer);
            Log.d("Stage2Model", "Model loaded from file, size: " + modelFile.length() + " bytes");
            return true;
        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load model from file: " + e.getMessage());
            return false;
        }
    }

    private void loadStage2Labels() {
        try {
            InputStream inputStream = context.getAssets().open("stage2_labels.txt");
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
            
            Log.d("Stage2Model", "Loaded " + stage2Labels.size() + " labels: " + stage2Labels);
        } catch (Exception e) {
            Log.e("Stage2Model", "Failed to load labels: " + e.getMessage());
            // Fallback labels
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

            // Preprocess image with Keras EfficientNet normalization
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
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

    public boolean isModelReady() {
        return isModelLoaded;
    }

    public void close() {
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }
}