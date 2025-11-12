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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
    private FirebaseFirestore firestore;
    private String deviceId;

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
        
        firestore = FirebaseFirestore.getInstance();
        deviceId = DeviceUtils.getDeviceId(this);
    }

    private void setupRecyclerView() {
        adapter = new RiceFieldAdapter(riceFields, this);
        rvRiceFields.setLayoutManager(new LinearLayoutManager(this));
        rvRiceFields.setAdapter(adapter);
    }

    private void loadRiceFields() {
        firestore.collection("users")
                .document(deviceId)
                .collection("rice_fields")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    riceFields = new ArrayList<>();
                    Gson gson = new Gson();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            RiceFieldProfile riceField = new RiceFieldProfile();
                            riceField.setId(document.getString("id"));
                            riceField.setName(document.getString("name"));
                               riceField.setImageUrl(document.getString("imageUrl"));
                               riceField.setProvince(document.getString("province"));
                               riceField.setCity(document.getString("city"));
                               riceField.setBarangay(document.getString("barangay"));
                               riceField.setSizeHectares(document.getDouble("sizeHectares") != null ? 
                                       document.getDouble("sizeHectares") : 0.0);
                               riceField.setSoilType(document.getString("soilType"));
                            
                            // Parse history from JSON string
                            String historyJson = document.getString("history");
                            if (historyJson != null && !historyJson.isEmpty()) {
                                Type listType = new TypeToken<List<RiceFieldProfile.HistoryEntry>>(){}.getType();
                                List<RiceFieldProfile.HistoryEntry> historyList = gson.fromJson(historyJson, listType);
                                if (historyList != null) {
                                    riceField.setHistory(historyList);
                                }
                            }
                            
                            riceFields.add(riceField);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    
                    adapter.updateList(riceFields);
                    
                    // Show/hide empty state
                    if (riceFields.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvRiceFields.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvRiceFields.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading rice fields: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    tvEmpty.setVisibility(View.VISIBLE);
                    rvRiceFields.setVisibility(View.GONE);
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
    
    private void showDeleteDialog(RiceFieldProfile riceField) {
        new AlertDialog.Builder(this)
                .setTitle("Tanggalin ang Palayan")
                .setMessage("Sigurado ka bang gusto mong tanggalin ang \"" + riceField.getName() + "\"?")
                .setPositiveButton("Tanggalin", (dialog, which) -> {
                    deleteRiceField(riceField);
                })
                .setNegativeButton("Kanselahin", null)
                .show();
    }
    
    private void deleteRiceField(RiceFieldProfile riceField) {
        JournalStorageHelper.deleteRiceField(this, riceField.getId(), new JournalStorageHelper.OnDeleteListener() {
            @Override
            public void onSuccess() {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), 
                    "Matagumpay na natanggal ang palayan!", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(FarmerJournal.this, R.color.green));
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTypeface(ResourcesCompat.getFont(FarmerJournal.this, R.font.poppins__regular));
                textView.setTextColor(ContextCompat.getColor(FarmerJournal.this, R.color.white));
                snackbar.show();
                
                // Reload list
                loadRiceFields();
            }

            @Override
            public void onFailure(String error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), 
                    "Hindi natanggal: " + error, Snackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(FarmerJournal.this, R.color.dark_red));
                TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTypeface(ResourcesCompat.getFont(FarmerJournal.this, R.font.poppins__regular));
                textView.setTextColor(ContextCompat.getColor(FarmerJournal.this, R.color.white));
                snackbar.show();
            }
        });
    }
}