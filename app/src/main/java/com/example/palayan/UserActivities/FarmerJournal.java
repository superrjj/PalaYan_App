package com.example.palayan.UserActivities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.palayan.R;
import com.example.palayan.databinding.ActivityFarmerJournalBinding;

public class FarmerJournal extends AppCompatActivity {

    private ActivityFarmerJournalBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityFarmerJournalBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

    }
}