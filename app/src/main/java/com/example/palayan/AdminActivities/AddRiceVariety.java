package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.LinkedHashMap;
import java.util.Map;

public class AddRiceVariety extends AppCompatActivity {

    private EditText varietyName, releaseName, breedingCode, yearRelease, breederOrigin, maturityDays, plantHeight, averageYield, maxYield, location, environment, season, plantingMethod;
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
        // Validate required field
        String name = varietyName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Variety Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        String id = databaseVarieties.push().getKey();

        // Store data in correct order
        Map<String, Object> variety = new LinkedHashMap<>();
        variety.put("rice_seed_id", id);
        variety.put("varietyName", name);
        variety.put("releaseName", releaseName.getText().toString().trim());
        variety.put("breedingCode", breedingCode.getText().toString().trim());
        variety.put("yearRelease", yearRelease.getText().toString().trim());
        variety.put("breederOrigin", breederOrigin.getText().toString().trim());
        variety.put("maturityDays", maturityDays.getText().toString().trim());
        variety.put("plantHeight", plantHeight.getText().toString().trim());
        variety.put("averageYield", averageYield.getText().toString().trim());
        variety.put("maxYield", maxYield.getText().toString().trim());
        variety.put("location", location.getText().toString().trim());
        variety.put("environment", environment.getText().toString().trim());
        variety.put("season", season.getText().toString().trim());
        variety.put("plantingMethod", plantingMethod.getText().toString().trim());

        // Save to Firebase
        databaseVarieties.child(id).setValue(variety)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rice Variety added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
