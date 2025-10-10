package com.example.palayan.UserActivities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.databinding.ActivityRiceVarietyInformationBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RiceVarietyInformation extends AppCompatActivity {

    private ActivityRiceVarietyInformationBinding root;
    private String riceSeedId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityRiceVarietyInformationBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();

        riceSeedId = getIntent().getStringExtra("rice_seed_id");

        if (riceSeedId != null) {
            loadData(riceSeedId);
        } else {
            Toast.makeText(this, "No rice variety ID provided.", Toast.LENGTH_SHORT).show();
            finish();
        }

        root.ivBack.setOnClickListener(view -> onBackPressed());
    }

    private void loadData(String riceSeedId) {
        Log.d("RiceVarietyInfo", "Loading data for rice_seed_id: " + riceSeedId);

        // Use document ID directly from adapter
        String documentId = getIntent().getStringExtra("document_id");

        if (documentId != null && !documentId.isEmpty()) {

            // Use document ID directly
            firestore.collection("rice_seed_varieties")
                    .document(documentId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {

                            try {
                                RiceVariety variety = documentSnapshot.toObject(RiceVariety.class);

                                if (variety != null) {

                                    // Set all fields
                                    setTextSafely(root.txtVarietyName, variety.varietyName);
                                    setTextSafely(root.txtReleaseName, variety.releaseName);
                                    setTextSafely(root.txtBreederCode, variety.breedingCode);
                                    setTextSafely(root.txtBreederOrigin, variety.breederOrigin);
                                    setTextSafely(root.txtYearRelease, variety.yearRelease);
                                    setTextSafely(root.txtLocation, variety.location);
                                    setTextSafely(root.txtPlantMethod, variety.plantingMethod);

                                    setTextSafely(root.txtAverageYield, variety.averageYield);
                                    setTextSafely(root.txtMaxYield, variety.maxYield);
                                    setTextSafely(root.txtMaturityDay, variety.maturityDays);
                                    setTextSafely(root.txtPlantHeight, variety.plantHeight);
                                    setTextSafely(root.txtTillers, variety.tillers);

                                    root.txtEnvironment.setText(listToString(variety.environment));
                                    root.txtSeason.setText(listToString(variety.season));

                                } else {
                                    Toast.makeText(this, "Failed to load variety data.", Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                Toast.makeText(this, "Error loading variety data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Variety not found.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "No document ID provided.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Helper method to safely set text
    private void setTextSafely(android.widget.TextView textView, String text) {
        if (textView != null) {
            textView.setText(text != null ? text : "");
        }
    }

    // Helper method to convert List<String> to display string
    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}