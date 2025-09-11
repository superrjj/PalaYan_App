package com.example.palayan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.databinding.ActivityPredictResultBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONObject;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PredictResult extends AppCompatActivity {

    private ActivityPredictResultBinding root;
    private Interpreter tflite;
    private JSONArray classesArray;
    private JSONObject metadataObj;

    GpuDelegate gpuDelegate = new GpuDelegate(); //GPU Tensorflow

    private static final int IMAGE_SIZE = 224;
    private Bitmap capturedBitmap; // store bitmap here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityPredictResultBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        gpuDelegate = new GpuDelegate();//initialize the gpu

        // Get image path from intent
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                capturedBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                root.ivDiseaseImage.setImageBitmap(capturedBitmap);
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image path passed", Toast.LENGTH_SHORT).show();
        }

        // Load model & metadata asynchronously
        loadModelAndMetadata(() -> {
            if (capturedBitmap != null) {
                runPrediction(capturedBitmap);
            }
        });
    }

    private void loadModelAndMetadata(Runnable onReady) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference modelRef = storage.getReference().child("rice_disease_model.tflite");
        File localModel = new File(getCacheDir(), "rice_disease_model.tflite");

        modelRef.getFile(localModel).addOnSuccessListener(task -> {
            try {
                tflite = new Interpreter(loadModelFile(localModel));

                // Load classes JSON
                StorageReference classRef = storage.getReference().child("rice_disease_classes.json");
                File localClasses = new File(getCacheDir(), "rice_disease_classes.json");

                classRef.getFile(localClasses).addOnSuccessListener(task2 -> {
                    try {
                        String json = readFile(localClasses);
                        classesArray = new JSONArray(json);

                        // Initialize tflite with GPU delegate here
                        Interpreter.Options options = new Interpreter.Options().addDelegate(gpuDelegate);
                        tflite = new Interpreter(loadModelFile(localModel), options);

                        // Load metadata JSON
                        StorageReference metaRef = storage.getReference().child("rice_disease_metadata.json");
                        File localMeta = new File(getCacheDir(), "rice_disease_metadata.json");

                        metaRef.getFile(localMeta).addOnSuccessListener(task3 -> {
                            try {
                                String metaJson = readFile(localMeta);
                                metadataObj = new JSONObject(metaJson);
                                onReady.run(); // only run prediction after everything is loaded
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }).addOnFailureListener(e -> Log.e("PredictResult", "Failed to download metadata", e));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).addOnFailureListener(e -> Log.e("PredictResult", "Failed to download classes.json", e));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(e -> Log.e("PredictResult", "Failed to download TFLite model", e));
    }


    private void runPrediction(Bitmap bitmap) {
        try {
            Bitmap resized = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3);
            inputBuffer.order(ByteOrder.nativeOrder());

            int[] pixels = new int[IMAGE_SIZE * IMAGE_SIZE];
            resized.getPixels(pixels, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

            for (int pixel : pixels) {
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                inputBuffer.putFloat(r / 255.0f);
                inputBuffer.putFloat(g / 255.0f);
                inputBuffer.putFloat(b / 255.0f);
            }

            float[][] output = new float[1][classesArray.length()];
            tflite.run(inputBuffer, output);

            int predictedIndex = argMax(output[0]);
            String predictedLabel = classesArray.getString(predictedIndex);

            JSONObject diseaseInfo = metadataObj.getJSONObject(predictedLabel);

            root.tvDiseaseName.setText(predictedLabel);
            root.tvSciName.setText(diseaseInfo.optString("scientific_name", ""));
            root.tvDiseaseDesc.setText(diseaseInfo.optString("description", ""));
            root.tvSymptoms.setText(diseaseInfo.optString("symptoms", ""));
            root.tvCause.setText(diseaseInfo.optString("cause", ""));
            root.tvTreatments.setText(diseaseInfo.optString("treatments", ""));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Prediction failed", Toast.LENGTH_LONG).show();
        }
    }

    private int argMax(float[] array) {
        int maxIndex = 0;
        float max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    private MappedByteBuffer loadModelFile(File modelFile) throws Exception {
        FileInputStream fis = new FileInputStream(modelFile);
        FileChannel fileChannel = fis.getChannel();
        long startOffset = 0;
        long declaredLength = modelFile.length();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private String readFile(File file) throws Exception {
        InputStream is = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        is.read(buffer);
        is.close();
        return new String(buffer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) tflite.close();
        if (gpuDelegate != null) gpuDelegate.close();
    }


}
