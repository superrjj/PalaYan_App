package com.example.palayan.UserActivities;

import android.app.DatePickerDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.palayan.Helper.RicePlanting;
import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
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
            showSnackBar("Invalid rice field.", false, null);
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
    
    private void deletePlanting() {
        if (plantingId == null || plantingId.isEmpty() || riceFieldId == null || riceFieldId.isEmpty()) {
            showSnackBar("Hindi ma-tanggal ang taniman.", false, null);
            return;
        }
        
        JournalStorageHelper.deleteRicePlanting(this, riceFieldId, plantingId, new JournalStorageHelper.OnDeleteListener() {
            @Override
            public void onSuccess() {
                showSnackBar("Matagumpay na natanggal ang taniman!", true, AddPlantingActivity.this::finish);
            }

            @Override
            public void onFailure(String error) {
                showSnackBar("Hindi natanggal: " + error, false, null);
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
            showSnackBar("Ilalagay ang barayti ng palay.", false, null);
            return;
        }
        if (!rbSabogTanim.isChecked() && !rbLipatTanim.isChecked()) {
            showSnackBar("Piliin ang paraan ng pagtatanim.", false, null);
            return;
        }
        if (plantingDate == null || plantingDate.isEmpty() || plantingDate.equals("Piliin ang petsa")) {
            showSnackBar("Piliin ang petsa ng pagtatanim.", false, null);
            return;
        }
        if (seedWeight.isEmpty()) {
            showSnackBar("Ilalagay ang kabuuang timbang ng binhi.", false, null);
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
                showSnackBar("Matagumpay na na-save ang taniman!", true, AddPlantingActivity.this::finish);
            }

            @Override
            public void onFailure(String error) {
                showSnackBar("Hindi na-save: " + error, false, null);
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

    private void showDeleteConfirmationDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_confirmation, null);
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        MaterialButton btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        MaterialButton btnDialogConfirm = dialogView.findViewById(R.id.btnDialogConfirm);

        tvDialogTitle.setText("Tanggalin ang Taniman");
        tvDialogMessage.setText("Sigurado ka bang gusto mong tanggalin ang taniman na ito?");
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
            deletePlanting();
        });

        dialog.show();
    }

    private void showSnackBar(String message, boolean isSuccess, @Nullable Runnable onDismiss) {
        View root = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(root, message, Snackbar.LENGTH_LONG);
        int backgroundColor = ContextCompat.getColor(this, isSuccess ? R.color.green : R.color.dark_red);
        snackbar.setBackgroundTint(backgroundColor);
        snackbar.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        if (textView != null) {
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins__regular));
        }

        if (onDismiss != null) {
            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    onDismiss.run();
                }
            });
        }

        snackbar.show();
    }
}

