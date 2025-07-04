package com.example.palayan;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.databinding.ActivityRiceVarietyInformationBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RiceVarietyInformation extends AppCompatActivity {

    private ActivityRiceVarietyInformationBinding root;
    private String riceSeedId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityRiceVarietyInformationBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();

        riceSeedId = getIntent().getStringExtra("rice_seed_id");
        if (riceSeedId != null) {
            loadData(riceSeedId);
        }

        root.ivBack.setOnClickListener(view -> onBackPressed());
    }

    private void loadData(String riceSeedId) {
        DocumentReference docRef = firestore.collection("rice_seed_varieties").document(riceSeedId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                RiceVariety variety = documentSnapshot.toObject(RiceVariety.class);

                if (variety != null) {
                    root.txtVarietyName.setText(variety.varietyName);
                    root.txtReleaseName.setText(variety.releaseName);
                    root.txtBreederCode.setText(variety.breedingCode);
                    root.txtBreederOrigin.setText(variety.breederOrigin);
                    root.txtYearRelease.setText(variety.yearRelease);
                    root.txtAverageYield.setText(String.valueOf(variety.averageYield));
                    root.txtMaxYield.setText(String.valueOf(variety.maxYield));
                    root.txtMaturityDay.setText(String.valueOf(variety.maturityDays));
                    root.txtPlantHeight.setText(String.valueOf(variety.plantHeight));
                    root.txtTillers.setText(String.valueOf(variety.tillers));
                    root.txtLocation.setText(variety.location);
                    root.txtEnvironment.setText(variety.environment);
                    root.txtSeason.setText(variety.season);
                    root.txtPlantMethod.setText(variety.plantingMethod);
                }

            } else {
                Toast.makeText(this, "Variety not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
