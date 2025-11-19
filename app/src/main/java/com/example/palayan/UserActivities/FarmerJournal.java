package com.example.palayan.UserActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Adapter.RiceFieldAdapter;
import com.example.palayan.Helper.AppHelper.DeviceUtils;
import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.Helper.RiceFieldProfile;
import com.example.palayan.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class FarmerJournal extends AppCompatActivity implements RiceFieldAdapter.OnRiceFieldClickListener {

    private RecyclerView rvRiceFields;
    private Button btnAddJournal;
    private ImageView ivBack;
    private TextView tvEmpty;
    private LinearLayout layoutBack;
    private RiceFieldAdapter adapter;
    private List<RiceFieldProfile> riceFields;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_journal);

        initViews();
        setupRecyclerView();
        loadRiceFields();
        setupListeners();
    }

    private void initViews() {
        rvRiceFields = findViewById(R.id.rvRiceFields);
        btnAddJournal = findViewById(R.id.btnAddJournal);
        ivBack = findViewById(R.id.ivBack);
        layoutBack = findViewById(R.id.layoutBack);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        // Removed Firestore - using local storage now
    }

    private void setupRecyclerView() {
        adapter = new RiceFieldAdapter(riceFields, this);
        rvRiceFields.setLayoutManager(new LinearLayoutManager(this));
        rvRiceFields.setAdapter(adapter);
    }

    private void loadRiceFields() {
        JournalStorageHelper.loadRiceFields(this, new JournalStorageHelper.OnFieldsLoadedListener() {
            @Override
            public void onSuccess(List<RiceFieldProfile> fields) {
                riceFields = fields != null ? fields : new ArrayList<>();
                adapter.updateList(riceFields);
                
                // Show/hide empty state
                if (riceFields.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvRiceFields.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvRiceFields.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(FarmerJournal.this, "Error loading rice fields: " + error, Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(View.VISIBLE);
                rvRiceFields.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        layoutBack.setOnClickListener(v -> finish());
        
        btnAddJournal.setOnClickListener(v -> {
            Intent intent = new Intent(FarmerJournal.this, AddFarmerJournal.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRiceFields();
    }

    @Override
    public void onRiceFieldClick(RiceFieldProfile riceField) {
        // Navigate to RiceFieldJournal with rice field data
        Intent intent = new Intent(FarmerJournal.this, RiceFieldJournal.class);
        intent.putExtra("riceFieldName", riceField.getName());
        intent.putExtra("riceFieldId", riceField.getId());
        startActivity(intent);
    }
}