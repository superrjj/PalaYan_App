package com.example.palayan;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.databinding.ActivityPredictResultBinding;

import java.io.File;

public class PredictResult extends AppCompatActivity {

    private ActivityPredictResultBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityPredictResultBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        // Get file path from intent
        String imagePath = getIntent().getStringExtra("imagePath");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                root.ivDiseaseImage.setImageBitmap(bitmap);
            }
        }
    }
}
