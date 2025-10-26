package com.example.palayan.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
import java.util.List;

public class Stage1ModelManager {
    private Interpreter tfliteInterpreter;
    private Context context;
    private boolean isModelLoaded = false;

    // Configuration constants
    private static final float CONFIDENCE_THRESHOLD = 0.7f; // Balanced threshold
    private static final int MIN_IMAGE_SIZE = 100;
    private static final double MIN_BLUR_SCORE = 30.0;
    private static final int INPUT_SIZE = 224;
    private static final int CHANNELS = 3;
    private List<String> stage1Labels = new ArrayList<>();

    // ImageNet normalization constants for EfficientNet
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    public Stage1ModelManager(Context context) {
        this.context = context;
        loadStage1Model();
        loadStage1Labels();
    }

    private void loadStage1Labels() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference labelsRef = storage.getReference().child("models/stage1_labels.txt");

        File localFile = new File(context.getFilesDir(), "stage1_labels.txt");

        // Delete old labels first
        if (localFile.exists()) {
            localFile.delete();
            Log.d("Stage1Model", "Deleted old stage1_labels.txt");
        }

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

                        stage1Labels.clear();
                        stage1Labels.addAll(labels);
                        Log.d("Stage1Model", "Loaded labels: " + stage1Labels);
                    } catch (Exception e) {
                        Log.e("Stage1Model", "Failed to read labels: " + e.getMessage());
                        // FIXED: Consistent fallback order
                        stage1Labels.add("non_rice_plant");  // Index 0
                        stage1Labels.add("rice_plant");      // Index 1
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Stage1Model", "Failed to download labels from Firebase: " + e.getMessage());
                    Log.d("Stage1Model", "Trying to load labels from assets as fallback...");
                    loadLabelsFromAssets();
                });
    }
    
    private void loadLabelsFromAssets() {
        try {
            Log.d("Stage1Model", "Loading labels from assets...");
            InputStream assetInputStream = context.getAssets().open("models/stage1_labels.txt");
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(assetInputStream));
            String line;
            stage1Labels.clear();
            while ((line = reader.readLine()) != null) {
                stage1Labels.add(line.trim());
            }
            reader.close();
            assetInputStream.close();
            Log.d("Stage1Model", "Loaded labels from assets: " + stage1Labels);
        } catch (Exception e) {
            Log.e("Stage1Model", "Failed to load labels from assets: " + e.getMessage());
            // Fallback to default labels
            stage1Labels.clear();
            stage1Labels.add("non_rice_plant");  // Index 0
            stage1Labels.add("rice_plant");      // Index 1
        }
    }

    private void loadStage1Model() {
        try {
            File localModelFile = new File(context.getFilesDir(), "stage1_rice_plant_classifier.tflite");
            Log.d("Stage1Model", "Checking for local model file: " + localModelFile.getAbsolutePath());
            Log.d("Stage1Model", "File exists: " + localModelFile.exists());

            if (localModelFile.exists()) {
                loadModelFromFile(localModelFile);
            } else {
                Log.d("Stage1Model", "Local model file not found, downloading from Firebase...");
                downloadStage1ModelFromFirebase();
            }

        } catch (Exception e) {
            Log.e("Stage1Model", "Failed to load Stage 1 model: " + e.getMessage());
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

            // Validate model
            if (validateModel()) {
                Log.d("Stage1Model", "Rice plant detection model loaded and validated successfully");
            } else {
                Log.e("Stage1Model", "Model validation failed");
                isModelLoaded = false;
            }

        } catch (Exception e) {
            Log.e("Stage1Model", "Failed to load model from file: " + e.getMessage());
            isModelLoaded = false;
        }
    }

    private void downloadStage1ModelFromFirebase() {
        Log.d("Stage1Model", "Starting to download Stage 1 model from Firebase Storage...");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference modelRef = storage.getReference().child("models/stage1_rice_plant_classifier.tflite");

        File localFile = new File(context.getFilesDir(), "stage1_rice_plant_classifier.tflite");

        // Delete old model first before downloading new one
        if (localFile.exists()) {
            boolean deleted = localFile.delete();
            Log.d("Stage1Model", "Deleted old model: " + deleted);
        }

        Log.d("Stage1Model", "Local file path: " + localFile.getAbsolutePath());

        modelRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Stage1Model", "Stage 1 model downloaded successfully");
                    Log.d("Stage1Model", "Downloaded file size: " + localFile.length() + " bytes");
                    loadModelFromFile(localFile);
                })
                .addOnFailureListener(exception -> {
                    Log.e("Stage1Model", "Failed to download Stage 1 model from Firebase: " + exception.getMessage());
                    Log.d("Stage1Model", "Trying to load from assets as fallback...");
                    loadModelFromAssets(); // Fallback to assets
                });
    }
    
    private void loadModelFromAssets() {
        try {
            Log.d("Stage1Model", "Loading Stage 1 model from assets...");
            InputStream assetInputStream = context.getAssets().open("models/stage1_rice_plant_classifier.tflite");
            File localFile = new File(context.getFilesDir(), "stage1_rice_plant_classifier.tflite");
            
            // Copy from assets to local storage
            FileOutputStream outputStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = assetInputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            assetInputStream.close();
            
            Log.d("Stage1Model", "Model loaded from assets successfully");
            loadModelFromFile(localFile);
        } catch (Exception e) {
            Log.e("Stage1Model", "Failed to load model from assets: " + e.getMessage());
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

            float[][] testOutput = new float[1][2];
            tfliteInterpreter.run(testInput, testOutput);

            // Check if output is valid (sum should be close to 1.0 for softmax)
            float sum = testOutput[0][0] + testOutput[0][1];
            boolean isValid = Math.abs(sum - 1.0f) < 0.1f;

            Log.d("Stage1Model", "Model validation - Output sum: " + sum + ", Valid: " + isValid);
            return isValid;

        } catch (Exception e) {
            Log.e("Stage1Model", "Model validation failed: " + e.getMessage());
            return false;
        }
    }

    public boolean detectRicePlant(String imagePath) {
        Log.d("Stage1Model", "=== STARTING IMPROVED RICE PLANT ANALYSIS ===");
        Log.d("Stage1Model", "Image path: " + imagePath);

        if (!isModelLoaded) {
            Log.e("Stage1Model", "Model not loaded - returning false");
            return false;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                Log.e("Stage1Model", "Failed to load image - returning false");
                return false;
            }

            Log.d("Stage1Model", "Original image size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // Image quality validation - SKIP for now to rely on ML model
            // ImageQualityResult qualityResult = validateImageQuality(bitmap);
            // if (!qualityResult.isGood) {
            //     Log.w("Stage1Model", "Image quality check failed: " + qualityResult.reason);
            //     return false;
            // }
            Log.d("Stage1Model", "Skipping image quality validation, relying on ML model");

            // Preprocess image with correct normalization
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
            ByteBuffer inputBuffer = preprocessImageCorrectly(resizedBitmap);

            // Run ML inference
            float[][] output = new float[1][2];
            long startTime = System.currentTimeMillis();
            tfliteInterpreter.run(inputBuffer, output);
            long inferenceTime = System.currentTimeMillis() - startTime;

            Log.d("Stage1Model", "ML inference completed in: " + inferenceTime + "ms");

            // CORRECT: Mapping based on training order
            float nonRiceConfidence = output[0][0];  // Index 0 = non_rice_plant
            float riceConfidence = output[0][1];     // Index 1 = rice_plant

            Log.d("Stage1Model", "Raw ML output - Rice: " + riceConfidence + ", NonRice: " + nonRiceConfidence);

            // Apply confidence threshold - RELAXED for debugging
            // Accept if rice confidence is greater than non-rice, even if below threshold
            boolean isRicePlant = riceConfidence > nonRiceConfidence;
            
            Log.d("Stage1Model", "Decision logic: Rice > NonRice = " + (riceConfidence > nonRiceConfidence));
            Log.d("Stage1Model", "Threshold check (Rice > " + CONFIDENCE_THRESHOLD + ") = " + (riceConfidence > CONFIDENCE_THRESHOLD));

            Log.d("Stage1Model", "=== FINAL DECISION ===");
            Log.d("Stage1Model", "Raw ML output array: [" + output[0][0] + ", " + output[0][1] + "]");
            Log.d("Stage1Model", "Rice confidence (index 1): " + riceConfidence + " (" + (riceConfidence * 100) + "%)");
            Log.d("Stage1Model", "NonRice confidence (index 0): " + nonRiceConfidence + " (" + (nonRiceConfidence * 100) + "%)");
            Log.d("Stage1Model", "Confidence threshold: " + CONFIDENCE_THRESHOLD);
            Log.d("Stage1Model", "Threshold met: " + (riceConfidence > CONFIDENCE_THRESHOLD));
            Log.d("Stage1Model", "Rice > NonRice: " + (riceConfidence > nonRiceConfidence));
            Log.d("Stage1Model", "Final result (Rice Plant): " + isRicePlant);
            Log.d("Stage1Model", "=== END ANALYSIS ===");

            return isRicePlant;

        } catch (Exception e) {
            Log.e("Stage1Model", "Analysis failed: " + e.getMessage());
            e.printStackTrace();
            return false;
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

    private ImageQualityResult validateImageQuality(Bitmap bitmap) {
        // Check minimum size
        if (bitmap.getWidth() < MIN_IMAGE_SIZE || bitmap.getHeight() < MIN_IMAGE_SIZE) {
            return new ImageQualityResult(false, "Image too small: " + bitmap.getWidth() + "x" + bitmap.getHeight());
        }

        // Check for blur
        double blurScore = calculateBlurScore(bitmap);
        if (blurScore < MIN_BLUR_SCORE) {
            return new ImageQualityResult(false, "Image too blurry: " + blurScore);
        }

        // Check for green content (rice plant indicator)
        double greenRatio = calculateGreenRatio(bitmap);
        if (greenRatio < 0.10) { // Relaxed - just need some green content
            return new ImageQualityResult(false, "Insufficient green content: " + greenRatio);
        }
        
        // Additional check: Rice plants should have some texture/variation, not too uniform
        double texture = calculateBlurScore(bitmap);
        if (texture > 5000) { // Relaxed - only reject very uniform images
            return new ImageQualityResult(false, "Image too uniform (likely not rice): " + texture);
        }
        
        // Check for specific rice plant characteristics (relaxed)
        RicePlantValidation validation = validateRicePlantCharacteristicsRelaxed(bitmap);
        if (!validation.isRicePlant) {
            return new ImageQualityResult(false, validation.reason);
        }

        return new ImageQualityResult(true, "Image quality is good");
    }

    private double calculateBlurScore(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        long sum = 0;
        for (int pixel : pixels) {
            int gray = ((pixel >> 16 & 0xff) + (pixel >> 8 & 0xff) + (pixel & 0xff)) / 3;
            sum += gray;
        }
        double mean = sum / (double) pixels.length;

        long varianceSum = 0;
        for (int pixel : pixels) {
            int gray = ((pixel >> 16 & 0xff) + (pixel >> 8 & 0xff) + (pixel & 0xff)) / 3;
            varianceSum += Math.pow(gray - mean, 2);
        }
        return varianceSum / (double) pixels.length;
    }

    private double calculateGreenRatio(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int totalPixels = width * height;
        int greenPixels = 0;

        int[] pixels = new int[totalPixels];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;

            // Check if pixel is predominantly green
            if (g > r && g > b && g > 100) {
                greenPixels++;
            }
        }
        return (double) greenPixels / totalPixels;
    }

    private RicePlantValidation validateRicePlantCharacteristics(Bitmap bitmap) {
        // Rice plants should have:
        // 1. Multiple variations in green (not uniform)
        // 2. Some yellow/brown variations (mature rice grains/bunga)
        // 3. Natural color distribution (not solid green like lettuce)
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int totalPixels = width * height;
        int[] pixels = new int[totalPixels];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        int lightGreenCount = 0;
        int darkGreenCount = 0;
        int yellowBrownCount = 0;
        int yellowCount = 0;
        
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;
            
            // Light green (young rice leaves)
            if (g > 140 && g > r && g > b && g - r < 40) {
                lightGreenCount++;
            }
            // Dark green (mature rice leaves)
            else if (g > 70 && g < 140 && g > r && g > b) {
                darkGreenCount++;
            }
            // Yellow (rice grains/bunga)
            else if (r > 180 && g > 150 && b < 150 && g > b) {
                yellowCount++;
            }
            // Yellow-brown (rice grains/bunga - mature)
            else if (r > 120 && g > 100 && b < 120 && r > g && r > b) {
                yellowBrownCount++;
            }
        }
        
        double lightGreenRatio = (double) lightGreenCount / totalPixels;
        double darkGreenRatio = (double) darkGreenCount / totalPixels;
        double yellowRatio = (double) yellowCount / totalPixels;
        double yellowBrownRatio = (double) yellowBrownCount / totalPixels;
        double totalGreenRatio = lightGreenRatio + darkGreenRatio;
        double totalRiceColorRatio = totalGreenRatio + yellowRatio + yellowBrownRatio;
        
        // Rice plants should have good variation
        boolean hasGreen = totalGreenRatio > 0.10; // At least 10% green
        boolean hasVariation = (lightGreenRatio > 0.02 || darkGreenRatio > 0.02) && totalRiceColorRatio > 0.15;
        
        Log.d("Stage1Model", "Color analysis - Green: " + String.format("%.1f", totalGreenRatio * 100) + 
              "%, Yellow: " + String.format("%.1f", yellowRatio * 100) + 
              "%, YellowBrown: " + String.format("%.1f", yellowBrownRatio * 100) + "%");
        
        // Reject if too little green (not a plant)
        if (!hasGreen) {
            return new RicePlantValidation(false, "Too little green content (" + String.format("%.1f", totalGreenRatio * 100) + "%)");
        }
        
        // Reject if uniform solid green (likely lettuce/cabbage)
        if (totalGreenRatio > 0.8 && yellowBrownRatio < 0.05) {
            return new RicePlantValidation(false, "Too uniform green, no rice characteristics (likely lettuce/cabbage)");
        }
        
        // Accept if has good rice plant color distribution
        if (hasVariation) {
            return new RicePlantValidation(true, "Rice plant characteristics detected (Green: " + 
                String.format("%.1f", totalGreenRatio * 100) + 
                "%, Grains: " + String.format("%.1f", (yellowRatio + yellowBrownRatio) * 100) + "%)");
        }
        
        return new RicePlantValidation(true, "Rice plant detected");
    }

    private RicePlantValidation validateRicePlantCharacteristicsRelaxed(Bitmap bitmap) {
        // More relaxed validation - accept images with rice characteristics
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int totalPixels = width * height;
        int[] pixels = new int[totalPixels];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        
        int greenCount = 0;
        int yellowCount = 0;
        
        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;
            
            // Green colors (rice leaves)
            if (g > 60 && g > r && g > b) {
                greenCount++;
            }
            // Yellow/golden colors (rice grains)
            else if (r > 140 && g > 140 && b < 120) {
                yellowCount++;
            }
        }
        
        double greenRatio = (double) greenCount / totalPixels;
        double yellowRatio = (double) yellowCount / totalPixels;
        
        Log.d("Stage1Model", "Relaxed validation - Green: " + String.format("%.1f", greenRatio * 100) + 
              "%, Yellow: " + String.format("%.1f", yellowRatio * 100) + "%");
        
        // Accept if has decent green content (rice leaves)
        if (greenRatio >= 0.10) {
            return new RicePlantValidation(true, "Rice plant characteristics detected");
        }
        
        return new RicePlantValidation(false, "Too little green content (" + String.format("%.1f", greenRatio * 100) + "%)");
    }
    
    private static class RicePlantValidation {
        boolean isRicePlant;
        String reason;
        
        RicePlantValidation(boolean isRice, String reason) {
            this.isRicePlant = isRice;
            this.reason = reason;
        }
    }

    public String getDetailedPrediction(String imagePath) {
        if (!isModelLoaded) {
            return "Model not loaded";
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                return "Failed to load image";
            }

            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
            ByteBuffer inputBuffer = preprocessImageCorrectly(resizedBitmap);
            float[][] output = new float[1][2];
            tfliteInterpreter.run(inputBuffer, output);

            // FIXED: Correct mapping based on training order
            float nonRiceConfidence = output[0][0];  // Index 0 = non_rice_plant
            float riceConfidence = output[0][1];     // Index 1 = rice_plant

            return String.format("Rice=%.3f, NonRice=%.3f, Threshold=%.3f, Pass=%s",
                    riceConfidence, nonRiceConfidence, CONFIDENCE_THRESHOLD,
                    riceConfidence > CONFIDENCE_THRESHOLD);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
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

    // Helper class for image quality results
    private static class ImageQualityResult {
        public boolean isGood;
        public String reason;

        public ImageQualityResult(boolean isGood, String reason) {
            this.isGood = isGood;
            this.reason = reason;
        }
    }
}