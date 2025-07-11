package com.example.palayan.AdminActivities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.palayan.Dialog.CustomDialogFragment;
import com.example.palayan.Dialog.StatusDialogFragment;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityAddRiceVarietyBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class AddRiceVariety extends AppCompatActivity {

    private ActivityAddRiceVarietyBinding root;
    private FirebaseFirestore firestore;
    private boolean isEditMode = false;

    // Added ChipGroups
    private ChipGroup chipGroupEnvironment, chipGroupSeason, chipGroupPlanting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityAddRiceVarietyBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();

        // Bind ChipGroups
        chipGroupEnvironment = findViewById(R.id.chipGroupEnvironment);
        chipGroupSeason = findViewById(R.id.chipGroupSeason);
        chipGroupPlanting = findViewById(R.id.chipGroupPlanting);
        setupChipListeners();

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

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

            root.btnUpdateVariety.setOnClickListener(view -> {
                String id = getIntent().getStringExtra("rice_seed_id");
                showUpdateConfirmationDialog(id);
            });
        }
    }

    private void setupChipListeners() {


        chipGroupEnvironment.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {

            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> checkedIds) {
                StringBuilder builder = new StringBuilder();
                for (int id : checkedIds) {
                    Chip chip = chipGroup.findViewById(id);
                    if (chip != null) {
                        builder.append(", ").append(chip.getText());
                    }
                }
            }
        });

        chipGroupSeason.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {

            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> checkedIds) {
                StringBuilder builder = new StringBuilder();
                for (int id : checkedIds) {
                    Chip chip = chipGroup.findViewById(id);
                    if (chip != null) {
                        builder.append(", ").append(chip.getText());
                    }
                }
            }
        });


        chipGroupPlanting.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {

            @Override
            public void onCheckedChanged(@NonNull ChipGroup chipGroup, @NonNull List<Integer> checkedIds) {
                StringBuilder builder = new StringBuilder();
                for (int id : checkedIds) {
                    Chip chip = chipGroup.findViewById(id);
                    if (chip != null) {
                        builder.append(", ").append(chip.getText());
                    }
                }
            }
        });

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
        String varietyName = root.txtVarietyName.getText().toString().trim();
        CustomDialogFragment.newInstance(
                "Update Rice Variety",
                "Are you sure you want to update \"" + varietyName + "\"?",
                "The changes will be saved and take effect immediately.",
                R.drawable.ic_edit,
                "UPDATE",
                (dialog, which) -> updateVariety()
        ).show(getSupportFragmentManager(), "UpdateConfirmDialog");
    }

    private void addVarietyToDatabase() {
        firestore.collection("rice_seed_varieties")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxId = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            int currentId = Integer.parseInt(doc.getId());
                            if (currentId > maxId) {
                                maxId = currentId;
                            }
                        } catch (NumberFormatException ignored) {}
                    }

                    int newId = maxId + 1;
                    String id = String.valueOf(newId);

                    try {
                        int maturity = Integer.parseInt(root.txtMaturity.getText().toString().trim());
                        int height = Integer.parseInt(root.txtPlantHeight.getText().toString().trim());
                        int tillers = Integer.parseInt(root.txtTillers.getText().toString().trim());
                        double avgYield = Double.parseDouble(root.txtAverageYield.getText().toString().trim());
                        double maxYieldVal = Double.parseDouble(root.txtMaxYield.getText().toString().trim());

                        // Get selected chips
                        String environment = getSelectedChipsText(chipGroupEnvironment);
                        String season = getSelectedChipsText(chipGroupSeason);
                        String plantingMethod = getSelectedChipsText(chipGroupPlanting);

                        // Validate chip selections
                        if (environment.isEmpty() || season.isEmpty() || plantingMethod.isEmpty()) {
                            Toast.makeText(this, "Please select Environment, Season, and Planting Method.", Toast.LENGTH_LONG).show();
                            return;
                        }

                        RiceVariety variety = new RiceVariety(
                                id,
                                root.txtVarietyName.getText().toString().trim(),
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
                                environment,
                                season,
                                plantingMethod,
                                false
                        );

                        firestore.collection("rice_seed_varieties")
                                .document(id)
                                .set(variety)
                                .addOnSuccessListener(aVoid -> {
                                    String varietyName = root.txtVarietyName.getText().toString().trim();
                                    showSuccessDialog("added", varietyName);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                );

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter valid numbers for Maturity, Height, and Yields", Toast.LENGTH_LONG).show();
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch IDs: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void updateVariety() {
        String id = getIntent().getStringExtra("rice_seed_id");

        try {
            int maturity = Integer.parseInt(root.txtMaturity.getText().toString().trim());
            int height = Integer.parseInt(root.txtPlantHeight.getText().toString().trim());
            int tillers = Integer.parseInt(root.txtTillers.getText().toString().trim());
            double avgYield = Double.parseDouble(root.txtAverageYield.getText().toString().trim());
            double maxYieldVal = Double.parseDouble(root.txtMaxYield.getText().toString().trim());

            // Get selected chips
            String environment = getSelectedChipsText(chipGroupEnvironment);
            String season = getSelectedChipsText(chipGroupSeason);
            String plantingMethod = getSelectedChipsText(chipGroupPlanting);

            if (environment.isEmpty() || season.isEmpty() || plantingMethod.isEmpty()) {
                Toast.makeText(this, "Please select Environment, Season, and Planting Method.", Toast.LENGTH_LONG).show();
                return;
            }

            RiceVariety updatedVariety = new RiceVariety(
                    id,
                    root.txtVarietyName.getText().toString().trim(),
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
                    environment,
                    season,
                    plantingMethod,
                    false
            );

            firestore.collection("rice_seed_varieties")
                    .document(id)
                    .set(updatedVariety)
                    .addOnSuccessListener(aVoid -> {
                        String varietyName = root.txtVarietyName.getText().toString().trim();
                        showSuccessDialog("updated", varietyName);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input for numbers", Toast.LENGTH_LONG).show();
        }
    }

    private String getSelectedChipsText(ChipGroup chipGroup) {
        StringBuilder selected = new StringBuilder();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) chipGroup.getChildAt(i);
            if (chip.isChecked()) {
                if (selected.length() > 0) selected.append(", ");
                selected.append(chip.getText().toString());
            }
        }
        return selected.toString();
    }

    private void showSuccessDialog(String action, String varietyName) {
        String title = "Rice Variety " + (action.equals("updated") ? "Updated" : "Added");
        String message = varietyName + " has been successfully " + action + ".";

        StatusDialogFragment.newInstance(
                        title,
                        message,
                        R.drawable.ic_success,
                        R.color.green
                ).setOnDismissListener(() -> finish())
                .show(getSupportFragmentManager(), "SuccessDialog");
    }
}
