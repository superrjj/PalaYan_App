package com.example.palayan.UserActivities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.palayan.Helper.JournalStorageHelper;
import com.example.palayan.Helper.RiceFieldProfile;
import com.example.palayan.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddFarmerJournal extends AppCompatActivity {

    private TextInputEditText etName, etSize, etRiceVariety, etPlantingDate;
    private TextInputLayout layoutName, layoutSize, layoutSoilType, layoutRiceVariety, layoutPlantingDate;
    private AutoCompleteTextView actvSoilType;
    private Button btnSave;
    private ImageView ivBack;
    
    private Calendar calendar;
    private SimpleDateFormat dateFormatter;
    private String[] soilTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_farmer_journal);

        initViews();
        setupSoilTypeDropdown();
        setupDatePicker();
        setupListeners();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        etName = findViewById(R.id.etName);
        etSize = findViewById(R.id.etSize);
        actvSoilType = findViewById(R.id.actvSoilType);
        etRiceVariety = findViewById(R.id.etRiceVariety);
        etPlantingDate = findViewById(R.id.etPlantingDate);
        
        layoutName = findViewById(R.id.layoutName);
        layoutSize = findViewById(R.id.layoutSize);
        layoutSoilType = findViewById(R.id.layoutSoilType);
        layoutRiceVariety = findViewById(R.id.layoutRiceVariety);
        layoutPlantingDate = findViewById(R.id.layoutPlantingDate);
        
        btnSave = findViewById(R.id.btnSave);
        
        calendar = Calendar.getInstance();
        dateFormatter = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        
        soilTypes = getResources().getStringArray(R.array.environment_array);
    }

    private void setupSoilTypeDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, soilTypes);
        actvSoilType.setAdapter(adapter);
    }

    private void setupDatePicker() {
        etPlantingDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) -> {
                calendar.set(selectedYear, selectedMonth, selectedDay);
                etPlantingDate.setText(dateFormatter.format(calendar.getTime()));
                layoutPlantingDate.setError(null);
            },
            year, month, day
        );
        
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        
        btnSave.setOnClickListener(v -> saveRiceField());
    }

    private void saveRiceField() {
        // Clear previous errors
        layoutName.setError(null);
        layoutSize.setError(null);
        layoutSoilType.setError(null);
        layoutRiceVariety.setError(null);
        layoutPlantingDate.setError(null);

        // Get values
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String sizeStr = etSize.getText() != null ? etSize.getText().toString().trim() : "";
        String soilType = actvSoilType.getText() != null ? actvSoilType.getText().toString().trim() : "";
        String riceVariety = etRiceVariety.getText() != null ? etRiceVariety.getText().toString().trim() : "";
        String plantingDate = etPlantingDate.getText() != null ? etPlantingDate.getText().toString().trim() : "";

        // Validate
        boolean isValid = true;

        if (name.isEmpty()) {
            layoutName.setError("Kailangan ang pangalan ng palayan");
            isValid = false;
        }

        if (sizeStr.isEmpty()) {
            layoutSize.setError("Kailangan ang sukat ng palayan");
            isValid = false;
        } else {
            try {
                double size = Double.parseDouble(sizeStr);
                if (size <= 0) {
                    layoutSize.setError("Ang sukat ay dapat mas malaki sa 0");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutSize.setError("Hindi wasto ang sukat");
                isValid = false;
            }
        }

        if (soilType.isEmpty()) {
            layoutSoilType.setError("Kailangan ang uri ng lupa");
            isValid = false;
        }

        if (riceVariety.isEmpty()) {
            layoutRiceVariety.setError("Kailangan ang uri ng palay");
            isValid = false;
        }

        if (plantingDate.isEmpty()) {
            layoutPlantingDate.setError("Kailangan ang petsa ng pagtatanim");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create and save rice field profile
        double size = Double.parseDouble(sizeStr);
        RiceFieldProfile riceField = new RiceFieldProfile(name, size, soilType, riceVariety, plantingDate);
        
        JournalStorageHelper.saveRiceField(this, riceField, new JournalStorageHelper.OnSaveListener() {
            @Override
            public void onSuccess() {
                // Show success message
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), 
                    "Matagumpay na na-save ang palayan!", Snackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.green));
                android.widget.TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTypeface(ResourcesCompat.getFont(AddFarmerJournal.this, R.font.poppins__regular));
                textView.setTextColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.white));
                
                snackbar.addCallback(new com.google.android.material.snackbar.Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                        Intent intent = new Intent(AddFarmerJournal.this, FarmerJournal.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                
                snackbar.show();
            }

            @Override
            public void onFailure(String error) {
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), 
                    "Hindi na-save: " + error, Snackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.dark_red));
                android.widget.TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTypeface(ResourcesCompat.getFont(AddFarmerJournal.this, R.font.poppins__regular));
                textView.setTextColor(ContextCompat.getColor(AddFarmerJournal.this, R.color.white));
                snackbar.show();
            }
        });
    }
}

