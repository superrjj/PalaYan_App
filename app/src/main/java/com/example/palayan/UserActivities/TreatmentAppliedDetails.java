package com.example.palayan.UserActivities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.palayan.R;
import com.example.palayan.databinding.ActivityTreatmentAppliedDetailsBinding;

public class TreatmentAppliedDetails extends AppCompatActivity {

    private ActivityTreatmentAppliedDetailsBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityTreatmentAppliedDetailsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());



    }
}