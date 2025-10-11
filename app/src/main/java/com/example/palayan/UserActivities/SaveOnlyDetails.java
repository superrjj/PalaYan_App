package com.example.palayan.UserActivities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.HistoryResult;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivitySaveOnlyDetailsBinding;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class SaveOnlyDetails extends AppCompatActivity {

    private static final String TAG = "SaveOnlyDetails";
    private ActivitySaveOnlyDetailsBinding root;
    private String historyId;
    private String deviceId;
    private String historyType;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivitySaveOnlyDetailsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();

        // Get data from intent
        historyId = getIntent().getStringExtra("history_id");
        deviceId = getIntent().getStringExtra("device_id");
        historyType = getIntent().getStringExtra("history_type");

        Log.d(TAG, "History ID: " + historyId);
        Log.d(TAG, "Device ID: " + deviceId);
        Log.d(TAG, "History Type: " + historyType);

        if (historyId != null && deviceId != null) {
            loadHistoryDetails();
        } else {
            Toast.makeText(this, "Missing data", Toast.LENGTH_SHORT).show();
            finish();
        }

        root.ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadHistoryDetails() {
        String collectionName = "prediction".equals(historyType) ? "predictions_result" : "treatment_notes";

        DocumentReference docRef = firestore.collection("users")
                .document(deviceId)
                .collection(collectionName)
                .document(historyId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                HistoryResult history = documentSnapshot.toObject(HistoryResult.class);

                if (history != null) {
                    // Set disease name
                    root.tvDiseaseName.setText(history.getDiseaseName());

                    // Set scientific name (if available, otherwise show device ID)
                    if (history.getDeviceId() != null) {
                        root.tvScientificName.setText("Device: " + history.getDeviceId());
                    } else {
                        root.tvScientificName.setText("Scan Result");
                    }

                    // Set description
                    if (history.getDescription() != null && !history.getDescription().trim().isEmpty()) {
                        root.tvDescription.setText(history.getDescription());
                    } else {
                        root.tvDescription.setText("No description available");
                    }

                    // Set causes
                    if (history.getCauses() != null && !history.getCauses().trim().isEmpty()) {
                        root.tvCause.setText(history.getCauses());
                    } else {
                        root.tvCause.setText("No cause information available");
                    }

                    // Set symptoms
                    if (history.getSymptoms() != null && !history.getSymptoms().trim().isEmpty()) {
                        root.tvSymptoms.setText(history.getSymptoms());
                    } else {
                        root.tvSymptoms.setText("No symptoms information available");
                    }

                    // Set treatments
                    if (history.getTreatments() != null && !history.getTreatments().trim().isEmpty()) {
                        root.tvTreatments.setText(history.getTreatments());
                    } else {
                        root.tvTreatments.setText("No treatment information available");
                    }

                    // Load image
                    String imageUrl = history.getImageUrl();
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.loading_image)
                                .into(root.ivDiseaseImage);
                    } else {
                        root.ivDiseaseImage.setImageResource(R.drawable.loading_image);
                    }

                    Log.d(TAG, "Successfully loaded history details");
                } else {
                    Log.e(TAG, "Failed to convert document to HistoryResult");
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Document does not exist");
                Toast.makeText(this, "Data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error loading history details: " + e.getMessage());
            Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}