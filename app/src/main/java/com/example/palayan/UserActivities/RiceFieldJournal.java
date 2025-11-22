package com.example.palayan.UserActivities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
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

import com.example.palayan.Adapter.RicePlantingAdapter;
import com.example.palayan.Helper.CropCalendarTask;
import com.example.palayan.Helper.RiceFieldProfile;
import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.R;
import com.example.palayan.Helper.JournalStorageHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.ArrayList;

public class RiceFieldJournal extends AppCompatActivity {

    private static final int MAX_ACTIVE_PLANTINGS = 2;

    private TextView tvRiceFieldName;
    private TextView tvEmpty;
    private TextView tvDeleteRiceField;
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
        tvDeleteRiceField = findViewById(R.id.tvDeleteRiceField);
        rvPlantings = findViewById(R.id.rvPlantings);

        plantingList = new ArrayList<>();
        plantingAdapter = new RicePlantingAdapter(plantingList, this::openPlantingEditor);
        rvPlantings.setLayoutManager(new LinearLayoutManager(this));
        rvPlantings.setAdapter(plantingAdapter);
        rvPlantings.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        tvDeleteRiceField.setVisibility(View.VISIBLE);

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
            // Allow up to two active plantings
            if (hasReachedPlantingLimit()) {
                Toast.makeText(RiceFieldJournal.this, "Dalawang taniman lang ang maaaring aktibo sa bawat palayan.", Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(RiceFieldJournal.this, AddPlantingActivity.class);
            i.putExtra("riceFieldId", riceFieldId);
            i.putExtra("riceFieldName", tvRiceFieldName.getText().toString());
            startActivity(i);
        });
        
        tvDeleteRiceField.setOnClickListener(v -> {
            if (riceFieldId != null && !riceFieldId.isEmpty()) {
                showDeleteDialog();
            }
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
                    btnAddPlanting.setEnabled(true);
                    btnAddPlanting.setAlpha(1.0f);
                    tvDeleteRiceField.setVisibility(View.VISIBLE);
                } else {
                    tvEmpty.setVisibility(View.GONE);
                    rvPlantings.setVisibility(View.VISIBLE);
                    tvDeleteRiceField.setVisibility(View.VISIBLE);
                    // Disable add button only when limit is reached
                    if (hasReachedPlantingLimit()) {
                        btnAddPlanting.setEnabled(false);
                        btnAddPlanting.setAlpha(0.5f);
                    } else {
                        btnAddPlanting.setEnabled(true);
                        btnAddPlanting.setAlpha(1.0f);
                    }
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

    private boolean hasReachedPlantingLimit() {
        if (plantingList == null || plantingList.isEmpty()) {
            return false;
        }

        int activePlantings = 0;
        for (RicePlanting planting : plantingList) {
            List<CropCalendarTask> tasks = planting.getCropCalendarTasks();
            if (tasks == null || tasks.isEmpty()) {
                // If no tasks, consider it active
                activePlantings++;
                if (activePlantings >= MAX_ACTIVE_PLANTINGS) {
                    return true;
                }
                continue;
            }

            // Check if all tasks are completed
            boolean allTasksCompleted = true;
            boolean harvestCompleted = false;

            for (CropCalendarTask task : tasks) {
                if (!task.isCompleted()) {
                    allTasksCompleted = false;
                }
                // Check if harvest task is completed
                if ("harvest".equals(task.getTaskType()) && task.isCompleted()) {
                    harvestCompleted = true;
                }
            }

            // Count as active if tasks are incomplete or harvest not completed
            if (!allTasksCompleted || !harvestCompleted) {
                activePlantings++;
                if (activePlantings >= MAX_ACTIVE_PLANTINGS) {
                    return true;
                }
            }
        }

        return false;
    }

    private void showDeleteDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        MaterialButton btnDialogConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        String riceFieldName = tvRiceFieldName.getText().toString();
        tvDialogTitle.setText("Tanggalin ang Palayan");
        tvDialogMessage.setText("Sigurado ka bang gusto mong tanggalin ang \"" + riceFieldName + "\"? Ang lahat ng taniman sa palayang ito ay matatanggal din.");
        btnDialogConfirm.setText("Tanggalin");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        btnDialogConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            deleteRiceField();
        });

        dialog.show();
    }

    private void deleteRiceField() {
        if (riceFieldId == null || riceFieldId.isEmpty()) {
            Toast.makeText(this, "Hindi ma-load ang palayan.", Toast.LENGTH_SHORT).show();
            return;
        }

        JournalStorageHelper.loadRiceFields(this, new JournalStorageHelper.OnFieldsLoadedListener() {
            @Override
            public void onSuccess(List<RiceFieldProfile> fields) {
                RiceFieldProfile riceField = null;
                for (RiceFieldProfile field : fields) {
                    if (field.getId().equals(riceFieldId)) {
                        riceField = field;
                        break;
                    }
                }

                if (riceField != null) {
                    JournalStorageHelper.deleteRiceField(RiceFieldJournal.this, riceFieldId, new JournalStorageHelper.OnDeleteListener() {
                        @Override
                        public void onSuccess() {
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Matagumpay na natanggal ang palayan!", Snackbar.LENGTH_SHORT);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(ContextCompat.getColor(RiceFieldJournal.this, R.color.green));
                            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTypeface(ResourcesCompat.getFont(RiceFieldJournal.this, R.font.poppins__regular));
                            textView.setTextColor(ContextCompat.getColor(RiceFieldJournal.this, R.color.white));
                            snackbar.show();

                            // Go back to FarmerJournal
                            finish();
                        }

                        @Override
                        public void onFailure(String error) {
                            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                    "Hindi natanggal: " + error, Snackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(ContextCompat.getColor(RiceFieldJournal.this, R.color.dark_red));
                            TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                            textView.setTypeface(ResourcesCompat.getFont(RiceFieldJournal.this, R.font.poppins__regular));
                            textView.setTextColor(ContextCompat.getColor(RiceFieldJournal.this, R.color.white));
                            snackbar.show();
                        }
                    });
                } else {
                    Toast.makeText(RiceFieldJournal.this, "Hindi mahanap ang palayan.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RiceFieldJournal.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}