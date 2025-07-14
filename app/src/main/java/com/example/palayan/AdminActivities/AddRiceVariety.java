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
import com.example.palayan.Helper.Validator.TextHelp;
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

    // ChipGroups
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

        //Editing enabled
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
            String environment = getIntent().getStringExtra("environment");
            selectChipsFromText(chipGroupEnvironment, environment);
            String season = getIntent().getStringExtra("season");
            selectChipsFromText(chipGroupSeason, season);
            String plantingMethod = getIntent().getStringExtra("plantingMethod");
            selectChipsFromText(chipGroupPlanting, plantingMethod);

            TextHelp.clearChipErrorOnSelect(chipGroupEnvironment, root.tvChipEnvironmentError);
            TextHelp.clearChipErrorOnSelect(chipGroupSeason, root.tvChipSeasonError);
            TextHelp.clearChipErrorOnSelect(chipGroupPlanting, root.tvChipMethodError);

            root.btnUpdateVariety.setOnClickListener(view -> {
                String id = getIntent().getStringExtra("rice_seed_id");
                showUpdateConfirmationDialog(id);
            });
        }

        //Live validation
        TextHelp.addValidation(root.layoutVarietyName, root.txtVarietyName, "Field required");
        TextHelp.addValidation(root.layoutReleaseName, root.txtReleaseName, "Field required");
        TextHelp.addValidation(root.layoutBreedingCode, root.txtBreedingCode, "Field required");
        TextHelp.addValidation(root.layoutYearRelease, root.txtYearRelease, "Field required");
        TextHelp.addValidation(root.layoutBreederOrigin, root.txtBreederOrigin, "Field required");
        TextHelp.addValidation(root.layoutMaturity, root.txtMaturity, "Field required");
        TextHelp.addValidation(root.layoutPlantHeight, root.txtPlantHeight, "Field required");
        TextHelp.addValidation(root.layoutAverageYield, root.txtAverageYield, "Field required");
        TextHelp.addValidation(root.layoutMaxYield, root.txtMaxYield, "Field required");
        TextHelp.addValidation(root.layoutTillers, root.txtTillers, "Field required");
        TextHelp.addValidation(root.layoutLocation, root.txtLocation, "Field required");


    }

    private void setupChipListeners() {
        chipGroupEnvironment.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {});
        chipGroupSeason.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {});
        chipGroupPlanting.setOnCheckedStateChangeListener((chipGroup, checkedIds) -> {});
    }

    private boolean validateAllFields() {
        boolean isValid = true;

        // Text fields
        isValid &= TextHelp.isFilled(root.layoutVarietyName, root.txtVarietyName, "Please enter variety name");
        isValid &= TextHelp.isFilled(root.layoutReleaseName, root.txtReleaseName, "Please enter release name");
        isValid &= TextHelp.isFilled(root.layoutBreedingCode, root.txtBreedingCode, "Please enter breeding code");
        isValid &= TextHelp.isFilled(root.layoutYearRelease, root.txtYearRelease, "Please enter year release");
        isValid &= TextHelp.isFilled(root.layoutBreederOrigin, root.txtBreederOrigin, "Please enter breeder/origin");
        isValid &= TextHelp.isFilled(root.layoutMaturity, root.txtMaturity, "Please enter maturity days");
        isValid &= TextHelp.isFilled(root.layoutPlantHeight, root.txtPlantHeight, "Please enter plant height");
        isValid &= TextHelp.isFilled(root.layoutAverageYield, root.txtAverageYield, "Please enter average yield");
        isValid &= TextHelp.isFilled(root.layoutMaxYield, root.txtMaxYield, "Please enter max yield");
        isValid &= TextHelp.isFilled(root.layoutTillers, root.txtTillers, "Please enter no. of tillers");
        isValid &= TextHelp.isFilled(root.layoutLocation, root.txtLocation, "Please enter location");

        // ChipGroups
        isValid &= TextHelp.validateChipGroup(chipGroupEnvironment, root.tvChipEnvironmentError, "Please select environment");
        isValid &= TextHelp.validateChipGroup(chipGroupSeason, root.tvChipSeasonError, "Please select season");
        isValid &= TextHelp.validateChipGroup(chipGroupPlanting, root.tvChipMethodError, "Please select planting method");

        return isValid;
    }

    private void showAddConfirmationDialog() {

        if (!validateAllFields()) return;
        String id = root.txtVarietyName.getText().toString().trim();


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

        if (!validateAllFields()) return;

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
                            if (currentId > maxId) maxId = currentId;
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

                        String environment = getSelectedChipsText(chipGroupEnvironment);
                        String season = getSelectedChipsText(chipGroupSeason);
                        String plantingMethod = getSelectedChipsText(chipGroupPlanting);



                        RiceVariety variety = new RiceVariety(
                                id,
                                root.txtVarietyName.getText().toString().trim(),
                                root.txtReleaseName.getText().toString().trim(),
                                root.txtBreedingCode.getText().toString().trim(),
                                root.txtYearRelease.getText().toString().trim(),
                                root.txtBreederOrigin.getText().toString().trim(),
                                maturity, height, avgYield, maxYieldVal, tillers,
                                root.txtLocation.getText().toString().trim(),
                                environment, season, plantingMethod, false
                        );

                        firestore.collection("rice_seed_varieties")
                                .document(id)
                                .set(variety)
                                .addOnSuccessListener(aVoid -> { showSuccessDialog("added", variety.getVarietyName());
                                    finish();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show());

                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Enter valid numbers for Maturity, Height, and Yields", Toast.LENGTH_LONG).show();
                    }

                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error occurred: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void updateVariety() {
        String id = getIntent().getStringExtra("rice_seed_id");

        try {
            int maturity = Integer.parseInt(root.txtMaturity.getText().toString().trim());
            int height = Integer.parseInt(root.txtPlantHeight.getText().toString().trim());
            int tillers = Integer.parseInt(root.txtTillers.getText().toString().trim());
            double avgYield = Double.parseDouble(root.txtAverageYield.getText().toString().trim());
            double maxYieldVal = Double.parseDouble(root.txtMaxYield.getText().toString().trim());

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
                    maturity, height, avgYield, maxYieldVal, tillers,
                    root.txtLocation.getText().toString().trim(),
                    environment, season, plantingMethod, false
            );

            firestore.collection("rice_seed_varieties")
                    .document(id)
                    .set(updatedVariety)
                    .addOnSuccessListener(aVoid -> {
                        showSuccessDialog("updated", updatedVariety.getVarietyName());
                        finish(); //dito nagerror kanina
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show());

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input for numbers", Toast.LENGTH_LONG).show();
        }
    }


    //selected id's of chip group
    private String getSelectedChipsText(ChipGroup chipGroup) {
        StringBuilder selected = new StringBuilder();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View view = chipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                if (chip.isChecked()) {
                    if (selected.length() > 0) selected.append(", ");
                    selected.append(chip.getText().toString());
                }
            }
        }
        return selected.toString();
    }


    //add the selected chip in the database
    private void selectChipsFromText(ChipGroup chipGroup, String chipTexts) {
        if (chipTexts == null || chipTexts.isEmpty()) return;

        String[] selectedTexts = chipTexts.split(", ");
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View view = chipGroup.getChildAt(i);
            if (view instanceof Chip) {
                Chip chip = (Chip) view;
                for (String text : selectedTexts) {
                    if (chip.getText().toString().equalsIgnoreCase(text.trim())) {
                        chip.setChecked(true);
                    }
                }
            }
        }
    }


    //Success dialog it's either update or add
    private void showSuccessDialog(String action, String varietyName) {
        String title = "Rice Variety " + (action.equals("updated") ? "Updated" : "Added");
        String message = varietyName + " has been successfully " + action + ".";

        StatusDialogFragment.newInstance(title, message, R.drawable.ic_success, R.color.green)
                .show(getSupportFragmentManager(), "SuccessDialog");
    }

}
