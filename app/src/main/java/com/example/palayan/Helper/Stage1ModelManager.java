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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Stage1ModelManager {
    private Interpreter tfliteInterpreter;
    private Context context;
    private boolean isModelLoaded = false;

    public Stage1ModelManager(Context context) {
        this.context = context;
        loadStage1Model();
    }

    private void loadStage1Model() {
        try {
            // Try to load from local storage first
            File localModelFile = new File(context.getFilesDir(), "stage1_rice_plant_classifier.tflite");
            Log.d("Stage1Model", "Checking for local model file: " + localModelFile.getAbsolutePath());
            Log.d("Stage1Model", "File exists: " + localModelFile.exists());

            if (localModelFile.exists()) {
                // Load from local storage
                FileInputStream inputStream = new FileInputStream(localModelFile);
                FileChannel fileChannel = inputStream.getChannel();
                MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, localModelFile.length());

                tfliteInterpreter = new Interpreter(buffer);
                isModelLoaded = true;

                Log.d("Stage1Model", "Rice plant detection model loaded from local storage");
            } else {
                Log.d("Stage1Model", "Local model file not found, downloading from Firebase...");
                // Download from Firebase Storage
                downloadStage1ModelFromFirebase();
            }

        } catch (Exception e) {
            Log.e("Stage1Model", "Failed to load Stage 1 model: " + e.getMessage());
            isModelLoaded = false;
        }
    }

    private void downloadStage1ModelFromFirebase() {
        Log.d("Stage1Model", "Starting to download Stage 1 model from Firebase Storage...");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference modelRef = storage.getReference().child("models/stage1_rice_plant_classifier.tflite");

        File localFile = new File(context.getFilesDir(), "stage1_rice_plant_classifier.tflite");
        Log.d("Stage1Model", "Local file path: " + localFile.getAbsolutePath());

        modelRef.getFile(localFile)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d("Stage1Model", "Stage 1 model downloaded successfully");
                    Log.d("Stage1Model", "Downloaded file size: " + localFile.length() + " bytes");
                    // Retry loading after download
                    loadStage1Model();
                })
                .addOnFailureListener(exception -> {
                    Log.e("Stage1Model", "Failed to download Stage 1 model: " + exception.getMessage());
                    isModelLoaded = false;
                });
    }

    public boolean detectRicePlant(String imagePath) {
        Log.d("Stage1Model", "=== STARTING RICE PLANT ANALYSIS ===");
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

            // Preprocess image for ML model
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            ByteBuffer inputBuffer = preprocessImage(resizedBitmap);

            // Run ML inference
            float[][] output = new float[1][2];
            long startTime = System.currentTimeMillis();
            tfliteInterpreter.run(inputBuffer, output);
            long inferenceTime = System.currentTimeMillis() - startTime;

            Log.d("Stage1Model", "ML inference completed in: " + inferenceTime + "ms");

            float riceConfidence = output[0][0];
            float nonRiceConfidence = output[0][1];

            Log.d("Stage1Model", "Raw ML output - Rice: " + riceConfidence + ", NonRice: " + nonRiceConfidence);

            // Decision logic: If rice confidence > non-rice confidence, it's a rice plant
            boolean isRicePlant = riceConfidence > nonRiceConfidence;

            Log.d("Stage1Model", "=== FINAL DECISION ===");
            Log.d("Stage1Model", "Final result (Rice Plant): " + isRicePlant);
            Log.d("Stage1Model", "=== END ANALYSIS ===");

            return isRicePlant;

        } catch (Exception e) {
            Log.e("Stage1Model", "Analysis failed: " + e.getMessage());
            e.printStackTrace();
            return false;
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

    public String getDetailedPrediction(String imagePath) {
        if (!isModelLoaded) {
            return "Model not loaded";
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                return "Failed to load image";
            }

            // Get ML prediction
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            ByteBuffer inputBuffer = preprocessImage(resizedBitmap);
            float[][] output = new float[1][2];
            tfliteInterpreter.run(inputBuffer, output);

            float riceConfidence = output[0][0];
            float nonRiceConfidence = output[0][1];

            return String.format("ML: Rice=%.3f, NonRice=%.3f", riceConfidence, nonRiceConfidence);

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public void close() {
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }
}