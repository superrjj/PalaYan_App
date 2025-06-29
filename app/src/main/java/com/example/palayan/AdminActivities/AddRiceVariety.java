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

    private Button btnAddVariety;
    private DatabaseReference databaseVarieties;

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

        btnAddVariety.setOnClickListener(view -> addVariety());
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
                    false // not archived
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
}
