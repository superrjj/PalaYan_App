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
import com.example.palayan.Helper.AppHelper.DeviceUtils;
import com.example.palayan.Helper.RiceFieldProfile;
import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.R;
import com.example.palayan.Helper.JournalStorageHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class RiceFieldJournal extends AppCompatActivity {

    private TextView tvRiceFieldName;
    private TextView tvEmpty;
    private ImageView ivBack;
    private LinearLayout layoutBack;
    private FirebaseFirestore firestore;
    private String deviceId;
    private String riceFieldId;
    private Button btnAddPlanting;
    private RecyclerView rvPlantings;
    private RicePlantingAdapter plantingAdapter;
    private List<RicePlanting> plantingList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rice_field_journal);

        firestore = FirebaseFirestore.getInstance();
        deviceId = DeviceUtils.getDeviceId(this);
        
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
        plantingAdapter = new RicePlantingAdapter(plantingList, planting -> {
            // TODO: open details or edit screen
        });
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
                // Load rice field from Firestore
                firestore.collection("users")
                        .document(deviceId)
                        .collection("rice_fields")
                        .document(riceFieldId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                try {
                                    Gson gson = new Gson();
                                    RiceFieldProfile riceField = new RiceFieldProfile();
                                    riceField.setId(documentSnapshot.getString("id"));
                                    riceField.setName(documentSnapshot.getString("name"));
                                    riceField.setImageUrl(documentSnapshot.getString("imageUrl"));
                                    riceField.setProvince(documentSnapshot.getString("province"));
                                    riceField.setCity(documentSnapshot.getString("city"));
                                    riceField.setBarangay(documentSnapshot.getString("barangay"));
                                    riceField.setSizeHectares(documentSnapshot.getDouble("sizeHectares") != null ? 
                                            documentSnapshot.getDouble("sizeHectares") : 0.0);
                                    riceField.setSoilType(documentSnapshot.getString("soilType"));
                                    
                                    // Parse history from JSON string
                                    String historyJson = documentSnapshot.getString("history");
                                    if (historyJson != null && !historyJson.isEmpty()) {
                                        Type listType = new TypeToken<List<RiceFieldProfile.HistoryEntry>>(){}.getType();
                                        List<RiceFieldProfile.HistoryEntry> historyList = gson.fromJson(historyJson, listType);
                                        if (historyList != null) {
                                            riceField.setHistory(historyList);
                                        }
                                    }
                                    
                                    // Display the rice field name
                                    if (riceField.getName() != null && !riceField.getName().isEmpty()) {
                                        tvRiceFieldName.setText(riceField.getName());
                                    }
                                    loadPlantings();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Error loading rice field data", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Rice field not found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error loading rice field: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}