package com.example.palayan.UserActivities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.palayan.databinding.ActivityViewResultDiseaseBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewResultDisease extends AppCompatActivity {

    private ActivityViewResultDiseaseBinding root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewResultDiseaseBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        root.ivBack.setOnClickListener(v -> onBackPressed());

        String diseaseName = getIntent().getStringExtra("diseaseName");
        if (diseaseName == null || diseaseName.trim().isEmpty()) {
            Toast.makeText(this, "Walang pangalan ng sakit.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        root.tvDiseaseName.setText(diseaseName);
        root.tvAboutDisease.setText("Ano ang " + diseaseName + "?");

        loadDiseaseDetails(diseaseName);
    }

    private void loadDiseaseDetails(String diseaseName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("rice_local_diseases")
                .whereEqualTo("name", diseaseName)
                .limit(1)
                .get()
                .addOnSuccessListener(snap -> {
                    if (snap.isEmpty()) {
                        Toast.makeText(this, "Walang detalye para sa " + diseaseName, Toast.LENGTH_LONG).show();
                        return;
                    }
                    Map<String, Object> m = snap.getDocuments().get(0).getData();
                    if (m == null) return;

                    root.tvLocalName.setText(safeString(m.get("localName")));
                    root.tvDescription.setText(safeString(m.get("description")));
                    root.tvAffectedPart.setText(safeString(m.get("affectedParts")));

                    root.tvSymptoms.setText(extractBulleted(m.get("symptoms")));
                    root.tvCause.setText(extractBulleted(m.get("cause")));
                    root.tvTreatments.setText(extractBulleted(m.get("treatments")));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String safeString(Object v) {
        if (v == null) return "";
        String s = String.valueOf(v);
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    // Accepts List<String> or String. If String, split by dot and bullet.
    private String extractBulleted(Object v) {
        if (v == null) return "";
        if (v instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> raw = (List<Object>) v;
            List<String> items = new ArrayList<>();
            for (Object o : raw) {
                String s = safeString(o).trim();
                if (!s.isEmpty()) items.add(s);
            }
            return toBullets(items);
        }
        String s = safeString(v);
        return toBullets(splitOnDots(s));
    }

    private List<String> splitOnDots(String s) {
        List<String> out = new ArrayList<>();
        for (String part : s.split("\\.")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) out.add(trimmed);
        }
        return out;
    }

    private String toBullets(List<String> items) {
        if (items.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append("• ").append(items.get(i));
            if (i < items.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }
}