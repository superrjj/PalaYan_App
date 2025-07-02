package com.example.palayan;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.databinding.ActivityRiceVarietyInformationBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RiceVarietyInformation extends AppCompatActivity {

    private ActivityRiceVarietyInformationBinding root;
    private String riceSeedId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityRiceVarietyInformationBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        riceSeedId = getIntent().getStringExtra("rice_seed_id");
        if (riceSeedId != null) {
            loadData(riceSeedId);
        }

        root.ivBack.setOnClickListener(view -> onBackPressed());

    }

    private void loadData(String riceSeedId) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rice_seed_varieties").child(riceSeedId);

        ref.get().addOnSuccessListener(snapshot ->{
            if(snapshot.exists()){
                RiceVariety variety = snapshot.getValue(RiceVariety.class);

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
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
        });

    }
}