package com.example.palayan.UserActivities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.Disease;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityDiseaseDetailsBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class DiseaseDetails extends AppCompatActivity {


    private ActivityDiseaseDetailsBinding root;
    private String diseaseId;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityDiseaseDetailsBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();

        diseaseId = getIntent().getStringExtra("disease_id");


        if (diseaseId != null) {
            loadDiseaseDetails(diseaseId);
        } else {
            Toast.makeText(this, "No disease ID provided", Toast.LENGTH_SHORT).show();
        }

        root.ivBack.setOnClickListener(view -> onBackPressed());
    }

    private void loadDiseaseDetails(String diseaseId) {
        DocumentReference docRef = firestore.collection("rice_local_diseases").document(diseaseId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {


            if (documentSnapshot.exists()) {

                Disease disease = documentSnapshot.toObject(Disease.class);


                if (disease != null) {
                    // Set texts with null checks
                    setTextSafely(root.tvDiseaseName, disease.getName());
                    setTextSafely(root.tvLocalName, disease.getLocalName());
                    setTextSafely(root.tvSciName, disease.getScientificName());
                    setTextSafely(root.tvDescription, disease.getDescription());
                    setTextSafely(root.tvSymptoms, formatAsBulletList(disease.getSymptoms()));
                    setTextSafely(root.tvCause, formatAsBulletList(disease.getCause()));
                    setTextSafely(root.tvTreatments, formatAsBulletList(disease.getTreatments()));

                    // ADDED: Set affected parts
                    setTextSafely(root.tvAffectedPart, formatAsBulletList(disease.getAffectedParts()));

                    // Load image
                    String imageUrl = disease.getMainImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.loading_image)
                                .into(root.ivDiseaseImage);
                    } else {

                    }
                } else {

                    Toast.makeText(this, "Failed to parse disease data", Toast.LENGTH_SHORT).show();
                }

            } else {

                Toast.makeText(this, "Disease not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {

            Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setTextSafely(android.widget.TextView textView, String text) {
        if (textView != null) {
            textView.setText(text != null ? text : "No data available");
        }
    }

    private String formatAsBulletList(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return "• No data available.";
        }

        String[] items = rawText.split("\\.");
        StringBuilder result = new StringBuilder();
        for (String item : items) {
            item = item.trim();
            if (!item.isEmpty()) {
                result.append("• ").append(item).append("\n");
            }
        }
        return result.toString();
    }

    // Helper method to format List<String> as bullet list (for affectedParts)
    private String formatAsBulletList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "• No data available.";
        }
        StringBuilder result = new StringBuilder();
        for (String item : items) {
            if (item != null && !item.trim().isEmpty()) {
                result.append("• ").append(item.trim()).append("\n");
            }
        }
        return result.toString();
    }
}