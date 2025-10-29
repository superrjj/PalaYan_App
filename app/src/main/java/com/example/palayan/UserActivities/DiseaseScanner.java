package com.example.palayan.UserActivities;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.palayan.Helper.Stage2ModelManager;
import com.example.palayan.databinding.ActivityDiseaseScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiseaseScanner extends AppCompatActivity {
    private static final int REQ_GALLERY_PERM = 102;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isCapturing = false;
    private boolean isFlashOn = false;
    private ActivityDiseaseScannerBinding root;
    private Stage2ModelManager stage2Manager;
    private Camera camera;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityDiseaseScannerBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        previewView = root.previewView;
        Button btnCapture = root.btnCapture;
        Button btnGallery = root.btnGallery;
        Button btnFlashlight = root.btnFlashlight;

        stage2Manager = new Stage2ModelManager(this);

        // Register single-select image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    try {
                        File cachePath = new File(getCacheDir(), "images");
                        cachePath.mkdirs();
                        File file = new File(cachePath, "selected_disease.jpg");

                        try (InputStream in = getContentResolver().openInputStream(uri);
                             OutputStream out = new java.io.FileOutputStream(file)) {
                            byte[] buf = new byte[8192];
                            int len;
                            while ((len = in.read(buf)) != -1) {
                                out.write(buf, 0, len);
                            }
                        }

                        analyzeDiseaseFromImage(file.getAbsolutePath());
                    } catch (Exception e) {
                        Toast.makeText(DiseaseScanner.this, "Failed to use selected image", Toast.LENGTH_SHORT).show();
                    }
                }
        );

// Camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCapture.setOnClickListener(v -> {
            if (!isCapturing) takePhoto();
        });

// Flashlight button
        btnFlashlight.setOnClickListener(v -> toggleFlashlight());

// Gallery button (single select, with runtime permission)
        btnGallery.setOnClickListener(v -> {
            String perm = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    ? Manifest.permission.READ_MEDIA_IMAGES
                    : Manifest.permission.READ_EXTERNAL_STORAGE;

            if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
// Permission already granted, launch gallery
                pickImageLauncher.launch("image/*");
            } else {
// Request permission first
                ActivityCompat.requestPermissions(this, new String[]{perm}, REQ_GALLERY_PERM);
            }
        });

        root.ivBack.setOnClickListener(v -> onBackPressed());
    }

    // Flashlight toggle functionality
    private void toggleFlashlight() {
        if (camera == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            isFlashOn = !isFlashOn;
            camera.getCameraControl().enableTorch(isFlashOn);

            String message = isFlashOn ? "Flashlight ON" : "Flashlight OFF";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

            Log.d("DiseaseScanner", "Flashlight toggled: " + isFlashOn);
        } catch (Exception e) {
            Log.e("DiseaseScanner", "Error toggling flashlight: " + e.getMessage());
            Toast.makeText(this, "Flashlight not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("DiseaseScanner", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        isCapturing = true;

        File cachePath = new File(getCacheDir(), "images");
        cachePath.mkdirs();
        File file = new File(cachePath, "captured_disease.jpg");

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        isCapturing = false;
                        analyzeDiseaseFromImage(file.getAbsolutePath());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        isCapturing = false;
                        Toast.makeText(DiseaseScanner.this,
                                "Capture failed: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void analyzeDiseaseFromImage(String imagePath) {
// Show simple toast instead of loading dialog
        Toast.makeText(this, "Analyzing disease...", Toast.LENGTH_SHORT).show();

        // Run analysis immediately without waiting
        new Thread(() -> {
            // Just check if ready, don't wait
            if (!stage2Manager.isModelReady()) {

            }

            if (stage2Manager.isModelReady()) {
                performDiseaseAnalysis(imagePath);
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Model not ready. Please try again.", Toast.LENGTH_SHORT).show();
                });
                Log.w("DiseaseScanner", "Model not ready yet, but proceeding anyway...");
            }

            // Always proceed with analysis
            performDiseaseAnalysis(imagePath);
        }).start();
    }

    private void performDiseaseAnalysis(String imagePath) {
        try {
            // Load bitmap
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            if (bitmap == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Could not load image", Toast.LENGTH_SHORT).show();
                });
                return;
            }

            // Run Stage 2 prediction
            Stage2ModelManager.DiseaseResult result = stage2Manager.predictDisease(bitmap);

            runOnUiThread(() -> {
                if (result != null && result.isSuccess) {
                    // Pass results to PredictResult
                    goToPredictResult(result, imagePath);
                } else {
                    Toast.makeText(this, "Disease analysis failed: " +
                            (result != null ? result.errorMessage : "Unknown error"), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("DiseaseScanner", "Error analyzing disease", e);
            runOnUiThread(() -> {
                Toast.makeText(this, "Error analyzing disease: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void goToPredictResult(Stage2ModelManager.DiseaseResult result, String imagePath) {
        Intent intent = new Intent(this, PredictResult.class);
        intent.putExtra("imagePath", imagePath);
        intent.putExtra("diseaseName", result.diseaseName);
        intent.putExtra("confidence", result.confidence);

        // Add null check for diseaseInfo
        if (result.diseaseInfo != null) {
            intent.putExtra("scientificName", result.diseaseInfo.scientificName);
            intent.putExtra("description", result.diseaseInfo.description);
            intent.putExtra("symptoms", result.diseaseInfo.symptoms);
            intent.putExtra("cause", result.diseaseInfo.cause);
            intent.putExtra("treatments", result.diseaseInfo.treatments);
        } else {
            // Provide default values if diseaseInfo is null
            intent.putExtra("scientificName", "Unknown");
            intent.putExtra("description", "No description available");
            intent.putExtra("symptoms", "No symptoms information available");
            intent.putExtra("cause", "No cause information available");
            intent.putExtra("treatments", "No treatment information available");
        }

        intent.putExtra("isFromStage2", true);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (stage2Manager != null) {
            stage2Manager.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_GALLERY_PERM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, now launch gallery
                pickImageLauncher.launch("image/*");
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}