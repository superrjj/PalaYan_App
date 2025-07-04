package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.palayan.R;
import com.example.palayan.databinding.ActivityViewAccountsBinding;

public class ViewAccounts extends AppCompatActivity {

    private ActivityViewAccountsBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewAccountsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

    root.ivBack.setOnClickListener(v -> onBackPressed());
    root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddAdminAccount.class)));

    }
}