package com.example.palayan.UserActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.palayan.PredictResult;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityCameraScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraScanner extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isCapturing = false;
    private TextView tvWarning;

    private ActivityCameraScannerBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityCameraScannerBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        previewView = findViewById(R.id.previewView);
        tvWarning = findViewById(R.id.tvWarning);
        Button btnCapture = findViewById(R.id.btnCapture);

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
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider()); // ✅ tama na

                imageCapture = new ImageCapture.Builder().build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraScanner", "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

   private void takePhoto() {
        isCapturing = true;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull ImageProxy imageProxy) {
                        isCapturing = false;
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
                        imageProxy.close();

                        if (bitmap != null) {
                            try {
                                // Save bitmap to cache dir as file
                                File cachePath = new File(getCacheDir(), "images");
                                cachePath.mkdirs(); // make dir if not exists
                                File file = new File(cachePath, "captured.png");
                                FileOutputStream stream = new FileOutputStream(file);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                                stream.close();

                                // Pass file URI instead of byte array
                                Intent intent = new Intent(CameraScanner.this, PredictResult.class);
                                intent.putExtra("imagePath", file.getAbsolutePath());
                                startActivity(intent);

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(CameraScanner.this,
                                        "Failed to save image", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CameraScanner.this,
                                    "Failed to capture image", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        isCapturing = false;
                        Toast.makeText(CameraScanner.this,
                                "Capture failed: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
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

    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ByteBuffer buffer = imageProxy.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private double calculateBlur(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        long sum = 0;
        for (int pixel : pixels) {
            int gray = (pixel >> 16 & 0xff) + (pixel >> 8 & 0xff) + (pixel & 0xff);
            sum += gray / 3;
        }
        double mean = sum / (double) pixels.length;

        long varianceSum = 0;
        for (int pixel : pixels) {
            int gray = (pixel >> 16 & 0xff) + (pixel >> 8 & 0xff) + (pixel & 0xff);
            varianceSum += Math.pow((gray / 3) - mean, 2);
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

            if (g > r && g > b) {
                sampleCount++;
            }
        }
        return (double) sampleCount / totalPixels;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
