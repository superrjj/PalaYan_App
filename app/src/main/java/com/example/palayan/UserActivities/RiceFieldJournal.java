package com.example.palayan.UserActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Adapter.RicePlantingAdapter;
import com.example.palayan.Helper.RiceFieldProfile;
import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.R;
import com.example.palayan.Helper.JournalStorageHelper;

import java.util.List;
import java.util.ArrayList;

public class RiceFieldJournal extends AppCompatActivity {

    private TextView tvRiceFieldName;
    private TextView tvEmpty;
    private ImageView ivBack;
    private LinearLayout layoutBack;
    private String riceFieldId;
    private Button btnAddPlanting;
    private RecyclerView rvPlantings;
    private RicePlantingAdapter plantingAdapter;
    private List<RicePlanting> plantingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rice_field_journal);

        // Removed Firestore - using local storage now
        
        initViews();
        setupListeners();
        loadRiceFieldFromFirestore();
    }

    private void initViews() {
        tvRiceFieldName = findViewById(R.id.tvRiceFieldName);
        ivBack = findViewById(R.id.ivBack);
        layoutBack = findViewById(R.id.layoutBack);
        btnAddPlanting = findViewById(R.id.btnAddPlanting);
        tvEmpty = findViewById(R.id.tvEmpty);
        rvPlantings = findViewById(R.id.rvPlantings);

        plantingList = new ArrayList<>();
        plantingAdapter = new RicePlantingAdapter(plantingList, this::openPlantingEditor);
        rvPlantings.setLayoutManager(new LinearLayoutManager(this));
        rvPlantings.setAdapter(plantingAdapter);
        rvPlantings.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);

        String passedName = getIntent().getStringExtra("riceFieldName");
        if (passedName != null && !passedName.isEmpty()) {
            tvRiceFieldName.setText(passedName);
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        layoutBack.setOnClickListener(v -> finish());
        btnAddPlanting.setOnClickListener(v -> {
            if (riceFieldId == null || riceFieldId.isEmpty()) {
                Toast.makeText(RiceFieldJournal.this, "Hindi ma-load ang palayan.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(RiceFieldJournal.this, AddPlantingActivity.class);
            i.putExtra("riceFieldId", riceFieldId);
            i.putExtra("riceFieldName", tvRiceFieldName.getText().toString());
            startActivity(i);
        });
    }

    private void loadRiceFieldFromFirestore() {
        Intent intent = getIntent();
        if (intent != null) {
            riceFieldId = intent.getStringExtra("riceFieldId");
            if (riceFieldId != null && !riceFieldId.isEmpty()) {
                // Load rice field from local storage
                JournalStorageHelper.loadRiceFields(this, new JournalStorageHelper.OnFieldsLoadedListener() {
                    @Override
                    public void onSuccess(List<RiceFieldProfile> fields) {
                        // Find the specific rice field
                        RiceFieldProfile riceField = null;
                        for (RiceFieldProfile field : fields) {
                            if (field.getId().equals(riceFieldId)) {
                                riceField = field;
                                break;
                            }
                        }
                        
                        if (riceField != null) {
                            // Display the rice field name
                            if (riceField.getName() != null && !riceField.getName().isEmpty()) {
                                tvRiceFieldName.setText(riceField.getName());
                            }
                            loadPlantings();
                        } else {
                            Toast.makeText(RiceFieldJournal.this, "Rice field not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(RiceFieldJournal.this, "Error loading rice field: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(this, "Invalid rice field ID", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (riceFieldId != null && !riceFieldId.isEmpty()) {
            loadPlantings();
        }
    }

    private void loadPlantings() {
        if (riceFieldId == null || riceFieldId.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvPlantings.setVisibility(View.GONE);
            return;
        }
        
        // Load only plantings for this specific rice field (filtered by riceFieldId)
        JournalStorageHelper.loadRicePlantings(this, riceFieldId, new JournalStorageHelper.OnPlantingsLoadedListener() {
            @Override
            public void onSuccess(List<RicePlanting> plantings) {
                // Filter plantings to ensure they belong to this rice field (double check)
                List<RicePlanting> filteredPlantings = new ArrayList<>();
                for (RicePlanting planting : plantings) {
                    if (planting.getRiceFieldId() != null && planting.getRiceFieldId().equals(riceFieldId)) {
                        filteredPlantings.add(planting);
                    }
                }
                
                plantingList = filteredPlantings;
                plantingAdapter.updateList(plantingList);

                if (plantingList == null || plantingList.isEmpty()) {
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvPlantings.setVisibility(View.GONE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvPlantings.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String error) {
                tvEmpty.setVisibility(View.VISIBLE);
                rvPlantings.setVisibility(View.GONE);
                Toast.makeText(RiceFieldJournal.this, "Error loading plantings: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openPlantingEditor(RicePlanting planting) {
        if (planting == null) {
            return;
        }
        Intent intent = new Intent(RiceFieldJournal.this, AddPlantingActivity.class);
        intent.putExtra("riceFieldId", riceFieldId);
        intent.putExtra("riceFieldName", tvRiceFieldName.getText().toString());
        intent.putExtra("plantingId", planting.getId());
        intent.putExtra("plantingVariety", planting.getRiceVarietyName());
        intent.putExtra("plantingMethod", planting.getPlantingMethod());
        intent.putExtra("plantingDate", planting.getPlantingDate());
        intent.putExtra("seedWeight", planting.getSeedWeight());
        intent.putExtra("fertilizerUsed", planting.getFertilizerUsed());
        intent.putExtra("fertilizerAmount", planting.getFertilizerAmount());
        intent.putExtra("notes", planting.getNotes() != null ? planting.getNotes() : "");
        intent.putExtra("showCropCalendar", true); // Flag to show crop calendar directly
        startActivity(intent);
    }
}