package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddRiceVariety extends AppCompatActivity {

    private EditText varietyName, releaseName, breedingCode, yearRelease, breederOrigin,
            maturityDays, plantHeight, averageYield, maxYield, location,
            environment, season, plantingMethod;

    private Button btnAddVariety, btnUpdate;
    private DatabaseReference databaseVarieties;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_rice_variety);

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

        databaseVarieties = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");

        varietyName = findViewById(R.id.txtVarietyName);
        releaseName = findViewById(R.id.txtReleaseName);
        breedingCode = findViewById(R.id.txtBreedingCode);
        yearRelease = findViewById(R.id.txtYearRelease);
        breederOrigin = findViewById(R.id.txtBreederOrigin);
        maturityDays = findViewById(R.id.txtMaturity);
        plantHeight = findViewById(R.id.txtPlantHeight);
        averageYield = findViewById(R.id.txtAverageYield);
        maxYield = findViewById(R.id.txtMaxYield);
        location = findViewById(R.id.txtLocation);
        environment = findViewById(R.id.txtEnvironment);
        season = findViewById(R.id.txtSeason);
        plantingMethod = findViewById(R.id.txtPlantingMethod);

        btnAddVariety = findViewById(R.id.btnAddVariety);
        btnUpdate = findViewById(R.id.btnUpdateVariety);

        btnAddVariety.setVisibility(View.VISIBLE);
        btnUpdate.setVisibility(View.GONE);

        btnAddVariety.setOnClickListener(view -> addVariety());

        //check if editing the details of rice
        if (getIntent().getBooleanExtra("isEdit", false)) {
            isEditMode = true;
            btnAddVariety.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);

            //to pre-fill fields
            varietyName.setText(getIntent().getStringExtra("varietyName"));
            varietyName.setEnabled(false); // Cannot edit ID
            releaseName.setText(getIntent().getStringExtra("releaseName"));
            breedingCode.setText(getIntent().getStringExtra("breedingCode"));
            yearRelease.setText(getIntent().getStringExtra("yearRelease"));
            breederOrigin.setText(getIntent().getStringExtra("breederOrigin"));
            maturityDays.setText(String.valueOf(getIntent().getIntExtra("maturityDays", 0)));
            plantHeight.setText(String.valueOf(getIntent().getIntExtra("plantHeight", 0)));
            averageYield.setText(String.valueOf(getIntent().getDoubleExtra("averageYield", 0)));
            maxYield.setText(String.valueOf(getIntent().getDoubleExtra("maxYield", 0)));
            location.setText(getIntent().getStringExtra("location"));
            environment.setText(getIntent().getStringExtra("environment"));
            season.setText(getIntent().getStringExtra("season"));
            plantingMethod.setText(getIntent().getStringExtra("plantingMethod"));

            btnUpdate.setOnClickListener(view -> updateVariety());
        }
    }

    private void addVariety() {
        String id = varietyName.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Variety Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int maturity = Integer.parseInt(maturityDays.getText().toString().trim());
            int height = Integer.parseInt(plantHeight.getText().toString().trim());
            double avgYield = Double.parseDouble(averageYield.getText().toString().trim());
            double maxYieldVal = Double.parseDouble(maxYield.getText().toString().trim());

            RiceVariety variety = new RiceVariety(
                    id,
                    id,
                    releaseName.getText().toString().trim(),
                    breedingCode.getText().toString().trim(),
                    yearRelease.getText().toString().trim(),
                    breederOrigin.getText().toString().trim(),
                    maturity,
                    height,
                    avgYield,
                    maxYieldVal,
                    location.getText().toString().trim(),
                    environment.getText().toString().trim(),
                    season.getText().toString().trim(),
                    plantingMethod.getText().toString().trim(),
                    false // Not archived
            );

            databaseVarieties.child(id).setValue(variety)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Variety added successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers for Maturity, Height, and Yields", Toast.LENGTH_LONG).show();
        }
    }

    private void updateVariety() {
        String id = getIntent().getStringExtra("rice_seed_id");

        try {
            int maturity = Integer.parseInt(maturityDays.getText().toString().trim());
            int height = Integer.parseInt(plantHeight.getText().toString().trim());
            double avgYield = Double.parseDouble(averageYield.getText().toString().trim());
            double maxYieldVal = Double.parseDouble(maxYield.getText().toString().trim());

            RiceVariety updatedVariety = new RiceVariety(
                    id,
                    varietyName.getText().toString().trim(),
                    releaseName.getText().toString().trim(),
                    breedingCode.getText().toString().trim(),
                    yearRelease.getText().toString().trim(),
                    breederOrigin.getText().toString().trim(),
                    maturity,
                    height,
                    avgYield,
                    maxYieldVal,
                    location.getText().toString().trim(),
                    environment.getText().toString().trim(),
                    season.getText().toString().trim(),
                    plantingMethod.getText().toString().trim(),
                    false
            );

            databaseVarieties.child(id).setValue(updatedVariety)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Variety updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input for numbers", Toast.LENGTH_LONG).show();
        }
    }
}
