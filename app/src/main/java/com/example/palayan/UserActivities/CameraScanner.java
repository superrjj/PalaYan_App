package com.example.palayan.UserActivities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.palayan.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraScanner extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private boolean isCapturing = false;
    private TextView tvWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_scanner);

        previewView = findViewById(R.id.previewView);
        tvWarning = findViewById(R.id.tvWarning);
        Button btnCapture = findViewById(R.id.btnCapture);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnCapture.setOnClickListener(v -> {
            if (!isCapturing) takePhoto();
        });

        Handler handler = new Handler();
        Runnable autoCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (previewView.getBitmap() != null && !isCapturing) {
                    Bitmap currentFrame = previewView.getBitmap();
                    autoScanIfClear(currentFrame);
                }
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(autoCheckRunnable, 3000);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        isCapturing = true;
        File photoFile = new File(getExternalFilesDir(null), new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".jpg");

        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                isCapturing = false;
                Toast.makeText(CameraScanner.this, "Captured and saved to: " + photoFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                // Optionally display a thumbnail or do something with the image here
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                isCapturing = false;
                Toast.makeText(CameraScanner.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void autoScanIfClear(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int centerSize = 300;
        Bitmap centerCrop = Bitmap.createBitmap(bitmap,
                (width - centerSize) / 2,
                (height - centerSize) / 2,
                centerSize,
                centerSize);

        int pixelCount = 0;
        int darkPixels = 0;

        for (int x = 0; x < centerCrop.getWidth(); x++) {
            for (int y = 0; y < centerCrop.getHeight(); y++) {
                int pixel = centerCrop.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);
                int brightness = (r + g + b) / 3;
                if (brightness < 70) darkPixels++;
                pixelCount++;
            }
        }

        float darkRatio = (float) darkPixels / pixelCount;

        if (darkRatio > 0.20f) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Auto-capturing...", Toast.LENGTH_SHORT).show();
                takePhoto();
            });
        } else {
            runOnUiThread(() -> {
                tvWarning.setText("Masyadong malayo ang object");
                tvWarning.setVisibility(View.VISIBLE);
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                if (vibrator != null) vibrator.vibrate(300);
                MediaPlayer mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI);
                mediaPlayer.start();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}
