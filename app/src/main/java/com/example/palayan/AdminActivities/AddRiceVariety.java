package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Dialog.StatusDialogFragment;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddRiceVarietyBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddRiceVariety extends AppCompatActivity {

    private ActivityAddRiceVarietyBinding root;
    private DatabaseReference databaseVarieties;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddRiceVarietyBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

        databaseVarieties = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");

        root.btnAddVariety.setVisibility(View.VISIBLE);
        root.btnUpdateVariety.setVisibility(View.GONE);

        root.btnAddVariety.setOnClickListener(view -> showAddConfirmationDialog());

        if (getIntent().getBooleanExtra("isEdit", false)) {
            isEditMode = true;
            root.btnAddVariety.setVisibility(View.GONE);
            root.btnUpdateVariety.setVisibility(View.VISIBLE);

            root.txtVarietyName.setText(getIntent().getStringExtra("varietyName"));
            root.txtVarietyName.setEnabled(false);
            root.txtReleaseName.setText(getIntent().getStringExtra("releaseName"));
            root.txtBreedingCode.setText(getIntent().getStringExtra("breedingCode"));
            root.txtYearRelease.setText(getIntent().getStringExtra("yearRelease"));
            root.txtBreederOrigin.setText(getIntent().getStringExtra("breederOrigin"));
            root.txtMaturity.setText(String.valueOf(getIntent().getIntExtra("maturityDays", 0)));
            root.txtPlantHeight.setText(String.valueOf(getIntent().getIntExtra("plantHeight", 0)));
            root.txtAverageYield.setText(String.valueOf(getIntent().getDoubleExtra("averageYield", 0)));
            root.txtMaxYield.setText(String.valueOf(getIntent().getDoubleExtra("maxYield", 0)));
            root.txtTillers.setText(String.valueOf(getIntent().getIntExtra("tillers", 0)));
            root.txtLocation.setText(getIntent().getStringExtra("location"));
            root.txtEnvironment.setText(getIntent().getStringExtra("environment"));
            root.txtSeason.setText(getIntent().getStringExtra("season"));
            root.txtPlantingMethod.setText(getIntent().getStringExtra("plantingMethod"));

            root.btnUpdateVariety.setOnClickListener(view -> {
                String id = getIntent().getStringExtra("rice_seed_id");
                showUpdateConfirmationDialog(id);
            });
        }
    }

    private void showAddConfirmationDialog() {
        String id = root.txtVarietyName.getText().toString().trim();

        if (id.isEmpty()) {
            Toast.makeText(this, "Variety Name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        CustomDialogFragment.newInstance(
                "Add Rice Variety",
                "Are you sure you want to add \"" + id + "\"?",
                "This rice variety will be added to the application and made available for selection and reporting",
                R.drawable.ic_rice_logo,
                "ADD",
                (dialog, which) -> addVarietyToDatabase()
        ).show(getSupportFragmentManager(), "AddConfirmDialog");
    }

    private void showUpdateConfirmationDialog(String id) {
        CustomDialogFragment.newInstance(
                "Update Rice Variety",
                "Are you sure you want to update \"" + id + "\"?",
                "The changes will be saved and take effect immediately.",
                R.drawable.ic_edit,
                "UPDATE",
                (dialog, which) -> updateVariety()
        ).show(getSupportFragmentManager(), "UpdateConfirmDialog");
    }

    private void addVarietyToDatabase() {
        String id = root.txtVarietyName.getText().toString().trim();

        try {
            int maturity = Integer.parseInt(root.txtMaturity.getText().toString().trim());
            int height = Integer.parseInt(root.txtPlantHeight.getText().toString().trim());
            int tillers = Integer.parseInt(root.txtTillers.getText().toString().trim());
            double avgYield = Double.parseDouble(root.txtAverageYield.getText().toString().trim());
            double maxYieldVal = Double.parseDouble(root.txtMaxYield.getText().toString().trim());

            RiceVariety variety = new RiceVariety(
                    id,
                    id,
                    root.txtReleaseName.getText().toString().trim(),
                    root.txtBreedingCode.getText().toString().trim(),
                    root.txtYearRelease.getText().toString().trim(),
                    root.txtBreederOrigin.getText().toString().trim(),
                    maturity,
                    height,
                    avgYield,
                    maxYieldVal,
                    tillers,
                    root.txtLocation.getText().toString().trim(),
                    root.txtEnvironment.getText().toString().trim(),
                    root.txtSeason.getText().toString().trim(),
                    root.txtPlantingMethod.getText().toString().trim(),
                    false
            );

            databaseVarieties.child(id).setValue(variety)
                    .addOnSuccessListener(aVoid -> showSuccessDialog("added", id))
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
            int maturity = Integer.parseInt(root.txtMaturity.getText().toString().trim());
            int height = Integer.parseInt(root.txtPlantHeight.getText().toString().trim());
            int tillers = Integer.parseInt(root.txtTillers.getText().toString().trim());
            double avgYield = Double.parseDouble(root.txtAverageYield.getText().toString().trim());
            double maxYieldVal = Double.parseDouble(root.txtMaxYield.getText().toString().trim());

            RiceVariety updatedVariety = new RiceVariety(
                    id,
                    id,
                    root.txtReleaseName.getText().toString().trim(),
                    root.txtBreedingCode.getText().toString().trim(),
                    root.txtYearRelease.getText().toString().trim(),
                    root.txtBreederOrigin.getText().toString().trim(),
                    maturity,
                    height,
                    avgYield,
                    maxYieldVal,
                    tillers,
                    root.txtLocation.getText().toString().trim(),
                    root.txtEnvironment.getText().toString().trim(),
                    root.txtSeason.getText().toString().trim(),
                    root.txtPlantingMethod.getText().toString().trim(),
                    false
            );

            databaseVarieties.child(id).setValue(updatedVariety)
                    .addOnSuccessListener(aVoid -> showSuccessDialog("updated", id))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input for numbers", Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccessDialog(String action, String name) {
        String title = "Rice Variety " + (action.equals("updated") ? "Updated" : "Added");
        String message = name + " has been successfully " + action + ".";

        StatusDialogFragment.newInstance(
                        title,
                        message,
                        R.drawable.ic_success,
                        R.color.green
                ).setOnDismissListener(() -> finish())
                .show(getSupportFragmentManager(), "SuccessDialog");
    }
}
