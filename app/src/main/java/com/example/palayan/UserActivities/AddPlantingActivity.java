package com.example.palayan.UserActivities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddPlantingActivity extends AppCompatActivity {

    private TextView tvTitle, tvRiceFieldInfo, tvPlantingDate, tvRemove;
    private ImageView ivBack;
    private LinearLayout layoutBack, layoutPlantingDate;
    private AutoCompleteTextView etRiceVariety;
    private TextInputEditText etSeedWeight, etFertilizerUsed, etFertilizerAmount;
    private RadioGroup rgPlantingMethod;
    private RadioButton rbSabogTanim, rbLipatTanim;
    private Button btnSave;

    private String riceFieldId;
    private String riceFieldName;
    private String plantingId; // For editing/deleting existing planting
    private String existingVariety;
    private String existingMethod;
    private String existingDate;
    private String existingSeedWeight;
    private String existingFertilizerUsed;
    private String existingFertilizerAmount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_planting);

        riceFieldId = getIntent().getStringExtra("riceFieldId");
        riceFieldName = getIntent().getStringExtra("riceFieldName");
        plantingId = getIntent().getStringExtra("plantingId");
        existingVariety = getIntent().getStringExtra("plantingVariety");
        existingMethod = getIntent().getStringExtra("plantingMethod");
        existingDate = getIntent().getStringExtra("plantingDate");
        existingSeedWeight = getIntent().getStringExtra("seedWeight");
        existingFertilizerUsed = getIntent().getStringExtra("fertilizerUsed");
        existingFertilizerAmount = getIntent().getStringExtra("fertilizerAmount");

        if (riceFieldId == null || riceFieldId.isEmpty()) {
            Toast.makeText(this, "Invalid rice field.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvRiceFieldInfo = findViewById(R.id.tvRiceFieldInfo);
        tvPlantingDate = findViewById(R.id.tvPlantingDate);
        ivBack = findViewById(R.id.ivBack);
        layoutBack = findViewById(R.id.layoutBack);
        layoutPlantingDate = findViewById(R.id.layoutPlantingDate);
        etRiceVariety = findViewById(R.id.etRiceVariety);
        etSeedWeight = findViewById(R.id.etSeedWeight);
        etFertilizerUsed = findViewById(R.id.etFertilizerUsed);
        etFertilizerAmount = findViewById(R.id.etFertilizerAmount);
        rgPlantingMethod = findViewById(R.id.rgPlantingMethod);
        rbSabogTanim = findViewById(R.id.rbSabogTanim);
        rbLipatTanim = findViewById(R.id.rbLipatTanim);
        btnSave = findViewById(R.id.btnSave);
        tvRemove = findViewById(R.id.tvRemove);

        if (riceFieldName != null && !riceFieldName.isEmpty()) {
            tvRiceFieldInfo.setText(getString(R.string.add_planting_for_field, riceFieldName));
        }

        ArrayAdapter<CharSequence> varietyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.sample_variety_array,
                android.R.layout.simple_list_item_1
        );
        etRiceVariety.setAdapter(varietyAdapter);
        etRiceVariety.setThreshold(1);

        populateExistingPlanting();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        layoutBack.setOnClickListener(v -> finish());

        layoutPlantingDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> savePlanting());
        
        tvRemove.setOnClickListener(v -> {
            if (plantingId != null && !plantingId.isEmpty()) {
                showDeleteConfirmationDialog();
            }
        });
    }
    
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tanggalin ang Taniman")
                .setMessage("Sigurado ka bang gusto mong tanggalin ang taniman na ito?")
                .setPositiveButton("Tanggalin", (dialog, which) -> {
                    deletePlanting();
                })
                .setNegativeButton("Kanselahin", null)
                .show();
    }
    
    private void deletePlanting() {
        if (plantingId == null || plantingId.isEmpty() || riceFieldId == null || riceFieldId.isEmpty()) {
            Toast.makeText(this, "Hindi ma-tanggal ang taniman.", Toast.LENGTH_SHORT).show();
            return;
        }
        
        JournalStorageHelper.deleteRicePlanting(this, riceFieldId, plantingId, new JournalStorageHelper.OnDeleteListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddPlantingActivity.this, "Matagumpay na natanggal ang taniman!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddPlantingActivity.this, "Hindi natanggal: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String displayDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    tvPlantingDate.setText(displayDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void savePlanting() {
        String variety = etRiceVariety.getText() != null ? etRiceVariety.getText().toString().trim() : "";
        String plantingDate = tvPlantingDate.getText().toString();
        String seedWeight = etSeedWeight.getText() != null ? etSeedWeight.getText().toString().trim() : "";
        String fertilizer = etFertilizerUsed.getText() != null ? etFertilizerUsed.getText().toString().trim() : "";
        String fertilizerAmount = etFertilizerAmount.getText() != null ? etFertilizerAmount.getText().toString().trim() : "";

        if (variety.isEmpty()) {
            Toast.makeText(this, "Ilalagay ang barayti ng palay.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!rbSabogTanim.isChecked() && !rbLipatTanim.isChecked()) {
            Toast.makeText(this, "Piliin ang paraan ng pagtatanim.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (plantingDate == null || plantingDate.isEmpty() || plantingDate.equals("Piliin ang petsa")) {
            Toast.makeText(this, "Piliin ang petsa ng pagtatanim.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (seedWeight.isEmpty()) {
            Toast.makeText(this, "Ilalagay ang kabuuang timbang ng binhi.", Toast.LENGTH_SHORT).show();
            return;
        }

        RicePlanting planting = new RicePlanting();
        planting.setRiceFieldId(riceFieldId);
        planting.setRiceVarietyName(variety);
        planting.setPlantingDate(plantingDate);
        planting.setSeedWeight(seedWeight);
        planting.setPlantingMethod(rbSabogTanim.isChecked() ? "Sabog-tanim" : "Lipat-tanim");
        planting.setFertilizerUsed(fertilizer);
        planting.setFertilizerAmount(fertilizerAmount);
        planting.setNotes(buildNotes(fertilizer, fertilizerAmount));
        if (plantingId != null && !plantingId.isEmpty()) {
            planting.setId(plantingId);
        }

        JournalStorageHelper.saveRicePlanting(this, riceFieldId, planting, new JournalStorageHelper.OnSaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(AddPlantingActivity.this, "Matagumpay na na-save ang taniman!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AddPlantingActivity.this, "Hindi na-save: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String buildNotes(String fertilizer, String amount) {
        if (fertilizer.isEmpty() && amount.isEmpty()) {
            return "";
        }
        if (!fertilizer.isEmpty() && !amount.isEmpty()) {
            return fertilizer + " - " + amount + " kg";
        }
        return !fertilizer.isEmpty() ? fertilizer : amount;
    }

    private void populateExistingPlanting() {
        boolean isEditing = plantingId != null && !plantingId.isEmpty();
        tvRemove.setVisibility(isEditing ? View.VISIBLE : View.GONE);

        if (!isEditing) {
            return;
        }

        tvTitle.setText("I-EDIT ANG TANIMAN");
        btnSave.setText("I-UPDATE ANG TANIMAN");

        if (existingVariety != null && !existingVariety.isEmpty()) {
            etRiceVariety.setText(existingVariety, false);
        }
        if (existingMethod != null && !existingMethod.isEmpty()) {
            if ("Sabog-tanim".equalsIgnoreCase(existingMethod)) {
                rbSabogTanim.setChecked(true);
            } else if ("Lipat-tanim".equalsIgnoreCase(existingMethod)) {
                rbLipatTanim.setChecked(true);
            }
        }
        if (existingDate != null && !existingDate.isEmpty()) {
            tvPlantingDate.setText(existingDate);
        }
        if (existingSeedWeight != null && !existingSeedWeight.isEmpty()) {
            etSeedWeight.setText(existingSeedWeight);
        }
        if (existingFertilizerUsed != null && !existingFertilizerUsed.isEmpty()) {
            etFertilizerUsed.setText(existingFertilizerUsed);
        }
        if (existingFertilizerAmount != null && !existingFertilizerAmount.isEmpty()) {
            etFertilizerAmount.setText(existingFertilizerAmount);
        }
    }
}

