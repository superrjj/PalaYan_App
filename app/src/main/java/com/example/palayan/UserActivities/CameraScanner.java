package com.example.palayan.UserActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.MeteringPoint;
import androidx.camera.core.MeteringPointFactory;
import androidx.camera.core.Preview;
import androidx.camera.core.SurfaceOrientedMeteringPointFactory;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.palayan.Helper.Stage1ModelManager;
import com.example.palayan.databinding.ActivityCameraScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraScanner extends AppCompatActivity {
    private static final int REQ_GALLERY_PERM = 102;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isCapturing = false;
    private TextView tvWarning;
    private ActivityCameraScannerBinding root;

    // Camera control for autofocus
    private Camera camera;
    private boolean isFlashOn = false;

    // Stage 1 detection variables
    private Stage1ModelManager stage1Manager;

    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityCameraScannerBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        previewView = root.previewView;
        tvWarning = root.tvWarning;
        Button btnCapture = root.btnCapture;
        Button btnGallery = root.btnGallery;
        Button btnFlashlight = root.btnFlashlight;

// Initialize Stage 1 model manager
        Log.d("CameraScanner", "🚀 INITIALIZING Stage1ModelManager...");
        stage1Manager = new Stage1ModelManager(this);
        Log.d("CameraScanner", "✅ Stage1ModelManager initialized");

// Set up tap to focus
        previewView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                focusOnTap(event.getX(), event.getY());
            }
            return true;
        });

// Register single-select image picker
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) return;
                    try {
                        File cachePath = new File(getCacheDir(), "images");
                        cachePath.mkdirs();
                        File file = new File(cachePath, "selected.jpg");

                        try (InputStream in = getContentResolver().openInputStream(uri);
                             OutputStream out = new FileOutputStream(file)) {
                            byte[] buf = new byte[8192];
                            int len;
                            while ((len = in.read(buf)) != -1) {
                                out.write(buf, 0, len);
                            }
                        }

// Check if it's a rice plant first
                        checkRicePlantAndProceed(file.getAbsolutePath());
                    } catch (Exception e) {
                        Toast.makeText(CameraScanner.this, "Failed to use selected image", Toast.LENGTH_SHORT).show();
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

// Gallery button - Fixed permission handling
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

            Log.d("CameraScanner", "Flashlight toggled: " + isFlashOn);
        } catch (Exception e) {
            Log.e("CameraScanner", "Error toggling flashlight: " + e.getMessage());
            Toast.makeText(this, "Flashlight not available", Toast.LENGTH_SHORT).show();
        }
    }

    // Tap to focus functionality
    private void focusOnTap(float x, float y) {
        if (camera == null) return;

        try {
// Create metering point factory
            MeteringPointFactory factory = new SurfaceOrientedMeteringPointFactory(
                    previewView.getWidth(), previewView.getHeight());

// Create metering point
            MeteringPoint point = factory.createPoint(x, y);

// Create focus action
            FocusMeteringAction action = new FocusMeteringAction.Builder(point)
                    .addPoint(point, FocusMeteringAction.FLAG_AF)
                    .addPoint(point, FocusMeteringAction.FLAG_AE)
                    .addPoint(point, FocusMeteringAction.FLAG_AWB)
                    .build();

// Start focus
            camera.getCameraControl().startFocusAndMetering(action);

// Show focus indicator
            Toast.makeText(this, "Focusing...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("CameraScanner", "Error focusing: " + e.getMessage());
        }
    }

    private void takePhoto() {
        Log.d("CameraScanner", "=== TAKING PHOTO ===");
        isCapturing = true;

        File cachePath = new File(getCacheDir(), "images");
        cachePath.mkdirs();
        File file = new File(cachePath, "captured.jpg");

        Log.d("CameraScanner", "Photo will be saved to: " + file.getAbsolutePath());

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        isCapturing = false;
                        Log.d("CameraScanner", "Photo saved successfully");
                        Log.d("CameraScanner", "File size: " + file.length() + " bytes");

// Check if it's a rice plant first
                        checkRicePlantAndProceed(file.getAbsolutePath());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        isCapturing = false;
                        Log.e("CameraScanner", "Photo capture failed: " + exception.getMessage());
                        Toast.makeText(CameraScanner.this,
                                "Capture failed: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Check rice plant and proceed - IMPROVED
    private void checkRicePlantAndProceed(String imagePath) {
// Show simple toast instead of loading dialog
        Toast.makeText(this, "Analyzing rice plant...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                Log.d("CameraScanner", "=== STARTING IMPROVED RICE PLANT ANALYSIS ===");
                Log.d("CameraScanner", "Image path: " + imagePath);

// Wait for model to be ready
                Log.d("CameraScanner", "⏳ WAITING for model to be ready...");
                int attempts = 0;
                while (!stage1Manager.isModelReady() && attempts < 30) {
                    Log.d("CameraScanner", "⏳ Attempt " + attempts + " - Model not ready yet...");
                    Thread.sleep(1000);
                    attempts++;
                }

                if (!stage1Manager.isModelReady()) {
                    Log.e("CameraScanner", "❌ MODEL NOT READY after 30 attempts!");
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Model not ready. Please try again.", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                Log.d("CameraScanner", "✅ MODEL IS READY! Proceeding to detection...");

// Run improved detection with try-catch to prevent crash
                final boolean[] isRicePlant = {false};
                try {
                    isRicePlant[0] = stage1Manager.detectRicePlant(imagePath);
// Get detailed prediction for debugging
                    String detailedPrediction = stage1Manager.getDetailedPrediction(imagePath);
                    Log.d("CameraScanner", "Detailed prediction: " + detailedPrediction);
                } catch (Exception e) {
                    Log.e("CameraScanner", "Stage 1 detection failed: " + e.getMessage());
                    e.printStackTrace();
// Bypass Stage 1 if model crashes - proceed to disease detection
                    isRicePlant[0] = true;
                    Log.w("CameraScanner", "Bypassing Stage 1 due to error - proceeding to Stage 2");
                }

                runOnUiThread(() -> {
                    Log.d("CameraScanner", "=== ANALYSIS RESULTS ===");
                    Log.d("CameraScanner", "Is Rice Plant detected: " + isRicePlant[0]);

                    if (isRicePlant[0]) {
// Rice plant detected - go to DiseaseScanner for disease analysis
                        Log.d("CameraScanner", "Rice plant detected - proceeding to DiseaseScanner...");
                        Intent intent = new Intent(CameraScanner.this, DiseaseScanner.class);
                        startActivity(intent);
                        finish();
                    } else {
// NonRice detected - show retry message with more details
                        Log.d("CameraScanner", "NonRice detected - showing retry message");
                        String message = "Not a rice plant detected.";
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e("CameraScanner", "Analysis failed: " + e.getMessage());
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Analysis failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
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

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraScanner", "Camera initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void analyzeFrame(ImageProxy imageProxy) {
        Bitmap bitmap = imageProxyToBitmap(imageProxy);
        imageProxy.close();

        if (bitmap != null) {
            double blurScore = calculateBlur(bitmap);
            double sizeRatio = calculateSizeRatio(bitmap);

            runOnUiThread(() -> {
                if (blurScore < 50) {
                    tvWarning.setText("Malabo ang kuha. Tiyakin malinaw ang larawan ng dahon.");
                    tvWarning.setVisibility(TextView.VISIBLE);
                } else if (sizeRatio < 0.2) {
                    tvWarning.setText("Masyadong malayo ang kuha. Lapitan pa ang dahon bago kunan.");
                    tvWarning.setVisibility(TextView.VISIBLE);
                } else {
                    tvWarning.setVisibility(TextView.GONE);
                }
            });
        }
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        try {
            if (imageProxy.getImage() == null) return null;

            if (imageProxy.getFormat() == ImageFormat.YUV_420_888) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                YuvImage yuvImage;
                ByteBuffer yBuffer = imageProxy.getPlanes()[0].getBuffer();
                ByteBuffer uBuffer = imageProxy.getPlanes()[1].getBuffer();
                ByteBuffer vBuffer = imageProxy.getPlanes()[2].getBuffer();

                int ySize = yBuffer.remaining();
                int uSize = uBuffer.remaining();
                int vSize = vBuffer.remaining();

                byte[] nv21 = new byte[ySize + uSize + vSize];

                yBuffer.get(nv21, 0, ySize);
                vBuffer.get(nv21, ySize, vSize);
                uBuffer.get(nv21, ySize + vSize, uSize);

                yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
                yuvImage.compressToJpeg(new Rect(0, 0, imageProxy.getWidth(), imageProxy.getHeight()), 90, out);

                byte[] imageBytes = out.toByteArray();
                return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private double calculateBlur(Bitmap bitmap) {
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

    private double calculateSizeRatio(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int totalPixels = width * height;

        int sampleCount = 0;
        int[] pixels = new int[totalPixels];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int pixel : pixels) {
            int r = (pixel >> 16) & 0xff;
            int g = (pixel >> 8) & 0xff;
            int b = pixel & 0xff;

            if (g > r && g > b) sampleCount++;
        }
        return (double) sampleCount / totalPixels;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (stage1Manager != null) {
            stage1Manager.close();
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