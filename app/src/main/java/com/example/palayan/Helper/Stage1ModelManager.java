package com.example.palayan.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.List;

public class Stage1ModelManager {
    private Interpreter tfliteInterpreter;
    private Context context;
    private boolean isModelLoaded = false;

    // Configuration constants
    private static final float CONFIDENCE_THRESHOLD = 0.2f; // More lenient to detect rice plants
    private static final int MIN_IMAGE_SIZE = 100;
    private static final double MIN_BLUR_SCORE = 30.0;
    private static final int INPUT_SIZE = 224;
    private static final int CHANNELS = 3;
    private List<String> stage1Labels = new ArrayList<>();
    private String lastCharacteristicsSummary = "";

    // ImageNet normalization constants for EfficientNet
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    public Stage1ModelManager(Context context) {
        this.context = context;
        loadStage1Model();
        loadStage1Labels();
    }

    private void loadStage1Model() {
        try {
            Log.d("Stage1Model", "Loading Stage 1 model...");

            // Try to load from assets first
            if (loadModelFromAssets()) {
                Log.d("Stage1Model", "Stage 1 model loaded from assets successfully");
                isModelLoaded = true;
                return;
            }

            // If assets loading fails, try to load from file
            if (loadModelFromFile()) {
                Log.d("Stage1Model", "Stage 1 model loaded from file successfully");
                isModelLoaded = true;
                return;
            }

            Log.e("Stage1Model", "Failed to load Stage 1 model from both assets and file");
            isModelLoaded = false;

        } catch (Exception e) {
            Log.e("Stage1Model", "Error loading Stage 1 model: " + e.getMessage());
            e.printStackTrace();
            isModelLoaded = false;
        }
    }

    private boolean loadModelFromAssets() {
        // Try both possible asset paths to avoid path mismatches
        final String[] candidates = new String[] {
                "models/stage1_rice_plant_classifier.tflite",
                "stage1_rice_plant_classifier.tflite"
        };

        for (String path : candidates) {
            try {
                Log.d("Stage1Model", "Trying to load from assets: " + path);
                InputStream inputStream = context.getAssets().open(path);
                byte[] modelBytes = new byte[inputStream.available()];
                int read = inputStream.read(modelBytes);
                inputStream.close();
                if (read <= 0) {
                    Log.w("Stage1Model", "Asset read 0 bytes for: " + path);
                    continue;
                }

                tfliteInterpreter = new Interpreter(modelBytes);
                Log.d("Stage1Model", "Model loaded from assets ('" + path + "'), size: " + modelBytes.length + " bytes");
                return true;
            } catch (Exception e) {
                Log.w("Stage1Model", "Not found in assets path: " + path + " -> " + e.getMessage());
            }
        }

        Log.e("Stage1Model", "Failed to load model from any asset path candidate");
        return false;
    }

    private boolean loadModelFromFile() {
        try {
            File modelFile = new File(context.getFilesDir(), "stage1_rice_plant_classifier.tflite");
            if (!modelFile.exists()) {
                Log.e("Stage1Model", "Model file does not exist: " + modelFile.getAbsolutePath());
                return false;
            }

            FileInputStream inputStream = new FileInputStream(modelFile);
            FileChannel fileChannel = inputStream.getChannel();
            MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());

            tfliteInterpreter = new Interpreter(buffer);
            Log.d("Stage1Model", "Model loaded from file, size: " + modelFile.length() + " bytes");
            return true;
        } catch (Exception e) {
            Log.e("Stage1Model", "Failed to load model from file: " + e.getMessage());
            return false;
        }
    }

    private void loadStage1Labels() {
        stage1Labels.clear();
        final String[] candidates = new String[] {
                "models/stage1_labels.txt",
                "stage1_labels.txt"
        };

        for (String path : candidates) {
            try {
                Log.d("Stage1Model", "Trying to load labels from assets: " + path);
                InputStream inputStream = context.getAssets().open(path);
                byte[] buffer = new byte[inputStream.available()];
                int read = inputStream.read(buffer);
                inputStream.close();
                if (read <= 0) {
                    Log.w("Stage1Model", "Labels read 0 bytes for: " + path);
                    continue;
                }

                String labelsContent = new String(buffer);
                String[] lines = labelsContent.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        stage1Labels.add(line.trim());
                    }
                }
                if (!stage1Labels.isEmpty()) {
                    Log.d("Stage1Model", "Loaded labels: " + stage1Labels);
                    return;
                }
            } catch (Exception e) {
                Log.w("Stage1Model", "Labels not found at: " + path + " -> " + e.getMessage());
            }
        }

        // Fallback labels
        Log.w("Stage1Model", "Using fallback labels");
        stage1Labels.add("non_rice_plant");
        stage1Labels.add("rice_plant");
    }

    public boolean detectRicePlant(String imagePath) {
        Log.d("Stage1Model", "=== STARTING RICE PLANT ANALYSIS ===");
        Log.d("Stage1Model", "Image path: " + imagePath);

        // Try to load bitmap early so we can run a heuristic fallback if needed
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap == null) {
            Log.e("Stage1Model", "Failed to load image - returning false");
            return false;
        }

        if (!isModelLoaded) {
            Log.w("Stage1Model", "Model not loaded - using heuristic fallback detection");
            boolean heuristic = heuristicRiceFallback(bitmap);
            Log.d("Stage1Model", "Heuristic fallback result: " + heuristic);
            return heuristic;
        }

        try {
            Log.d("Stage1Model", "Original image size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // Preprocess image with Keras EfficientNet normalization
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

            // Apply more lenient decision rule and combine with heuristic + rice characteristics
            boolean isRicePlant = false;
            boolean heuristic = heuristicRiceFallback(bitmap);
            boolean characteristics = checkPlantCharacteristics(bitmap);
            if (riceConfidence > nonRiceConfidence) {
                isRicePlant = true;
                Log.d("Stage1Model", "Decision: rice > nonRice");
            }
            if (riceConfidence > CONFIDENCE_THRESHOLD) {
                isRicePlant = true;
                Log.d("Stage1Model", "Decision: rice > threshold " + CONFIDENCE_THRESHOLD);
            }
            // If moderately confident and heuristic supports, accept
            if (!isRicePlant && riceConfidence > 0.15f && heuristic) {
                isRicePlant = true;
                Log.d("Stage1Model", "Decision: rice > 0.15 and heuristic true");
            }
            // If ML uncertain but rice characteristics strong, accept
            if (!isRicePlant && characteristics) {
                isRicePlant = true;
                Log.d("Stage1Model", "Decision: rice characteristics matched");
            }
            // If model outputs are both very low, rely on heuristic only
            if (!isRicePlant && riceConfidence < 0.1f && nonRiceConfidence < 0.1f) {
                Log.w("Stage1Model", "⚠️ Both confidences low -> heuristic only");
                isRicePlant = heuristic;
            }

            Log.d("Stage1Model", "=== FINAL DECISION ===");
            Log.d("Stage1Model", "Rice confidence (index 1): " + riceConfidence + " (" + (riceConfidence * 100) + "%)");
            Log.d("Stage1Model", "NonRice confidence (index 0): " + nonRiceConfidence + " (" + (nonRiceConfidence * 100) + "%)");
            Log.d("Stage1Model", "Confidence threshold: " + CONFIDENCE_THRESHOLD);
            Log.d("Stage1Model", "Threshold met: " + (riceConfidence > CONFIDENCE_THRESHOLD));
            Log.d("Stage1Model", "Rice > NonRice: " + (riceConfidence > nonRiceConfidence));
            Log.d("Stage1Model", "Final result (Rice Plant): " + isRicePlant);
            Log.d("Stage1Model", "🎯 RESULT: " + (isRicePlant ? "RICE PLANT ✅" : "NOT RICE ❌"));

            // Show quick color summary to the user
            try {
                android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                final String summary = lastCharacteristicsSummary;
                mainHandler.post(() -> {
                    if (summary != null && !summary.isEmpty()) {
                        android.widget.Toast.makeText(context, "Colors: " + summary, android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception ignore) {}

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

    // Very fast heuristic fallback: detects rice-like scenes by green dominance and some yellow
    private boolean heuristicRiceFallback(Bitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int total = width * height;
            int[] pixels = new int[total];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            int greenCount = 0;
            int yellowCount = 0;

            for (int p : pixels) {
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                if (g > r && g > b && g > 90) greenCount++;
                else if (r > 170 && g > 150 && b < 140) yellowCount++;
            }

            double greenRatio = (double) greenCount / total;
            double yellowRatio = (double) yellowCount / total;

            Log.d("Stage1Model", String.format("Heuristic ratios -> green: %.3f yellow: %.3f", greenRatio, yellowRatio));
            // Accept if clearly plant-like (enough green), or harvest-like (some yellow present)
            return greenRatio >= 0.12 || (greenRatio >= 0.08 && yellowRatio >= 0.04);
        } catch (Exception e) {
            Log.e("Stage1Model", "Heuristic fallback failed: " + e.getMessage());
            return false;
        }
    }

    // Rice-specific characteristics check with detailed logs (green leaves, yellow panicles, golden harvest)
    private boolean checkPlantCharacteristics(Bitmap bitmap) {
        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int totalPixels = width * height;
            int[] pixels = new int[totalPixels];
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

            int greenCount = 0;
            int yellowCount = 0;
            int goldenCount = 0;
            int textureVariation = 0;
            int riceLeafCount = 0;

            for (int i = 0; i < pixels.length; i++) {
                int pixel = pixels[i];
                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // Green rice leaves
                if (g > 100 && g > r && g > b && g > 80) {
                    greenCount++;
                    riceLeafCount++;
                }
                // Yellow panicles
                else if (r > 180 && g > 180 && b < 150 && Math.abs(r - g) < 30) {
                    yellowCount++;
                }
                // Golden near-harvest
                else if (r > 200 && g > 180 && b < 120 && r > g) {
                    goldenCount++;
                }

                if (i > 0) {
                    int prev = pixels[i - 1];
                    int pr = (prev >> 16) & 0xff;
                    int pg = (prev >> 8) & 0xff;
                    int pb = prev & 0xff;
                    int diff = Math.abs(r - pr) + Math.abs(g - pg) + Math.abs(b - pb);
                    if (diff > 40) textureVariation++;
                }
            }

            double greenRatio = (double) greenCount / totalPixels;
            double yellowRatio = (double) yellowCount / totalPixels;
            double goldenRatio = (double) goldenCount / totalPixels;
            double textureRatio = (double) textureVariation / totalPixels;
            double riceLeafRatio = (double) riceLeafCount / totalPixels;

            Log.d("Stage1Model", "Rice analysis - Green: " + String.format("%.1f", greenRatio * 100) +
                    "%, Yellow: " + String.format("%.1f", yellowRatio * 100) +
                    "%, Golden: " + String.format("%.1f", goldenRatio * 100) +
                    "%, Texture: " + String.format("%.1f", textureRatio * 100) +
                    "%, Rice Leaves: " + String.format("%.1f", riceLeafRatio * 100) + "%");

            // Save summary for UI toast/debug
            lastCharacteristicsSummary = String.format(
                    "Green %.1f%% | Yellow %.1f%% | Golden %.1f%%",
                    greenRatio * 100, yellowRatio * 100, goldenRatio * 100);

            boolean hasRiceColors = (greenRatio > 0.2) || (yellowRatio > 0.08) || (goldenRatio > 0.08);
            boolean hasRiceTexture = textureRatio > 0.15;
            boolean hasRiceLeaves = riceLeafRatio > 0.12;
            boolean hasEnoughGreen = greenRatio > 0.12;

            boolean isRice = hasRiceColors && hasRiceTexture && hasRiceLeaves && hasEnoughGreen;
            Log.d("Stage1Model", "Rice characteristics: Colors=" + hasRiceColors +
                    ", Texture=" + hasRiceTexture + ", Leaves=" + hasRiceLeaves +
                    ", Green=" + hasEnoughGreen + ", Result=" + isRice);
            return isRice;
        } catch (Exception e) {
            Log.e("Stage1Model", "Rice characteristics check failed: " + e.getMessage());
            return false;
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
}