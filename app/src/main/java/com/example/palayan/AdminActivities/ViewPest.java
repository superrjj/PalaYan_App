package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.databinding.ActivityViewPestBinding;


public class ViewPest extends AppCompatActivity {

    private ActivityViewPestBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewPestBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddPest.class)));
        root.ivBack.setOnClickListener(v -> onBackPressed());

    }
}