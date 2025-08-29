package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.palayan.R;
import com.example.palayan.databinding.ActivityViewDiseaseBinding;

public class ViewDisease extends AppCompatActivity {

    private ActivityViewDiseaseBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewDiseaseBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddRiceDisease.class)));
        root.ivBack.setOnClickListener(v -> onBackPressed());

    }
}