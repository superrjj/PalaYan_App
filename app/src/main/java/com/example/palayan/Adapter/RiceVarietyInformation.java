package com.example.palayan.Adapter;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.palayan.R;
import com.example.palayan.databinding.ActivityRiceVarietyInformationBinding;

public class RiceVarietyInformation extends AppCompatActivity {

    private ActivityRiceVarietyInformationBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityRiceVarietyInformationBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());



    }
}