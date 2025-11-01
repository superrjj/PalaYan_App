package com.example.palayan.UserActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Adapter.TreatmentTimelineAdapter;
import com.example.palayan.Helper.TreatmentTimelineModel;
import com.example.palayan.R;
import com.example.palayan.databinding.ActivityTreatmentAppliedDetailsBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TreatmentAppliedDetails extends AppCompatActivity {

    private ActivityTreatmentAppliedDetailsBinding root;
    private TreatmentTimelineAdapter treatmentTimelineAdapter;
    private List<TreatmentTimelineModel> treatmentTimelineList;

    private String historyId;
    private String deviceId;
    private String historyType;
    private String diseaseName;
    private String localName;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityTreatmentAppliedDetailsBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get intent data
        getIntentData();

        // Initialize views
        initializeViews();

        // Setup click listeners
        setupClickListeners();

        // Load data
        loadDiseaseInfo();
        loadTreatmentTimeline();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        historyId = intent.getStringExtra("history_id");
        deviceId = intent.getStringExtra("device_id");
        historyType = intent.getStringExtra("history_type");

        Log.d("TreatmentAppliedDetails", "Intent data - historyId: " + historyId +
                ", deviceId: " + deviceId + ", historyType: " + historyType);

        // Add validation
        if (historyId == null || deviceId == null) {
            Log.e("TreatmentAppliedDetails", "Missing required data - historyId: " + historyId + ", deviceId: " + deviceId);
            Toast.makeText(this, "Missing required data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        // Initialize combined treatment timeline
        treatmentTimelineList = new ArrayList<>();
        treatmentTimelineAdapter = new TreatmentTimelineAdapter(this, treatmentTimelineList);
        root.rvTreatmentTimeline.setLayoutManager(new LinearLayoutManager(this));
        root.rvTreatmentTimeline.setAdapter(treatmentTimelineAdapter);

        treatmentTimelineAdapter.setOnPhotoClickListener((imageUrl, date, description) -> {
            showPhotoDetailsDialog(imageUrl, date, description);
        });
    }

    private void setupClickListeners() {
        // Back button
        root.ivBack.setOnClickListener(v -> onBackPressed());
        // Update note button -> open TreatmentNotes in update mode
        root.btnUpdateProgress.setOnClickListener(v -> {
            Intent i = new Intent(this, TreatmentNotes.class);
            i.putExtra("history_id", historyId);
            i.putExtra("device_id", deviceId);
            i.putExtra("diseaseName", diseaseName);
            startActivity(i);
        });
    }

    private void loadDiseaseInfo() {
        if (historyId != null && deviceId != null) {
            Log.d("TreatmentAppliedDetails", "Loading disease info from treatment_notes for historyId: " + historyId);

            // Load disease information from treatment_notes collection
            // Based on Firebase structure: users/{deviceId}/treatment_notes/{documentId}
            db.collection("users")
                    .document(deviceId)
                    .collection("treatment_notes")
                    .whereEqualTo("documentId", historyId)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("TreatmentAppliedDetails", "Disease info query completed. Documents: " + queryDocumentSnapshots.size());

                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            diseaseName = document.getString("diseaseName");
                            localName = document.getString("localName");

                            Log.d("TreatmentAppliedDetails", "Disease info from treatment_notes - diseaseName: " + diseaseName + ", localName: " + localName);

                            // Update UI
                            root.tvDiseaseName.setText(diseaseName != null ? diseaseName : "Unknown Disease");
                            root.tvLocalName.setText(localName != null ? localName : "Unknown Local Name");
                        } else {
                            Log.w("TreatmentAppliedDetails", "No treatment_notes found for historyId: " + historyId);
                            // Fallback: try to load from predictions_result
                            loadDiseaseInfoFromPredictions();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TreatmentAppliedDetails", "Error loading disease info from treatment_notes: " + e.getMessage());
                        // Fallback: try to load from predictions_result
                        loadDiseaseInfoFromPredictions();
                    });
        } else {
            Log.e("TreatmentAppliedDetails", "Cannot load disease info - missing historyId or deviceId");
        }
    }

    private void loadDiseaseInfoFromPredictions() {
        Log.d("TreatmentAppliedDetails", "Fallback: Loading disease info from predictions_result for historyId: " + historyId);

        // Fallback: Load disease information from predictions_result
        db.collection("treatment_notes")
                .document(historyId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d("TreatmentAppliedDetails", "Fallback disease info query completed");
                    if (documentSnapshot.exists()) {
                        diseaseName = documentSnapshot.getString("diseaseName");
                        localName = documentSnapshot.getString("localName");

                        Log.d("TreatmentAppliedDetails", "Fallback disease info - diseaseName: " + diseaseName + ", localName: " + localName);

                        // Update UI
                        root.tvDiseaseName.setText(diseaseName != null ? diseaseName : "Unknown Disease");
                        root.tvLocalName.setText(localName != null ? localName : "Unknown Local Name");
                    } else {
                        Log.w("TreatmentAppliedDetails", "No disease info found in predictions_result for historyId: " + historyId);
                        Toast.makeText(this, "Disease information not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TreatmentAppliedDetails", "Error loading fallback disease info: " + e.getMessage());
                    Toast.makeText(this, "Error loading disease information: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadTreatmentTimeline() {
        if (historyId != null && deviceId != null) {
            Log.d("TreatmentAppliedDetails", "Loading treatment timeline for historyId: " + historyId + ", deviceId: " + deviceId);

            // Load combined timeline from treatment_notes collection
            db.collection("users")
                    .document(deviceId)
                    .collection("treatment_notes")
                    .whereEqualTo("documentId", historyId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("TreatmentAppliedDetails", "Treatment timeline query completed. Documents: " + queryDocumentSnapshots.size());

                        treatmentTimelineList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String photoUrl = document.getString("photoUrl");
                            String description = document.getString("description");
                            String dateApplied = document.getString("dateApplied");
                            Date timestamp = document.getDate("timestamp");

                            Log.d("TreatmentAppliedDetails", "Timeline item - photoUrl: " + photoUrl + ", description: " + description + ", dateApplied: " + dateApplied + ", timestamp: " + timestamp);

                            // Include entry if it has at least photo OR description, and has a date
                            if ((photoUrl != null || (description != null && !description.isEmpty())) && (dateApplied != null || timestamp != null)) {
                                String formattedDate = dateApplied != null ? dateApplied : formatDate(timestamp);
                                TreatmentTimelineModel timelineItem = new TreatmentTimelineModel(
                                    photoUrl != null ? photoUrl : "",
                                    formattedDate,
                                    description != null ? description : "",
                                    timestamp
                                );
                                treatmentTimelineList.add(timelineItem);
                            }
                        }

                        // Sort by timestamp (newest first) in memory
                        treatmentTimelineList.sort((a, b) -> {
                            Date dateA = a.getTimestamp();
                            Date dateB = b.getTimestamp();
                            if (dateA == null && dateB == null) return 0;
                            if (dateA == null) return 1;
                            if (dateB == null) return -1;
                            return dateB.compareTo(dateA); // Descending order (newest first)
                        });

                        treatmentTimelineAdapter.notifyDataSetChanged();

                        if (treatmentTimelineList.isEmpty()) {
                            Log.d("TreatmentAppliedDetails", "No treatment timeline entries found");
                            Toast.makeText(this, "Walang timeline entries", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("TreatmentAppliedDetails", "Loaded " + treatmentTimelineList.size() + " timeline entries");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TreatmentAppliedDetails", "Error loading treatment timeline: " + e.getMessage());
                        Toast.makeText(this, "Error loading timeline: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.e("TreatmentAppliedDetails", "Cannot load treatment timeline - missing historyId or deviceId");
        }
    }

    private String formatDate(Date date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC+8"));
            return sdf.format(date);
        } catch (Exception e) {
            Log.e("TreatmentAppliedDetails", "Date formatting error: " + e.getMessage());
            return "Invalid Date";
        }
    }

    private void showPhotoDetailsDialog(String imageUrl, String date, String description) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_photo_details, null);
        
        ImageView ivPhotoFull = dialogView.findViewById(R.id.ivPhotoFull);
        TextView tvPhotoDate = dialogView.findViewById(R.id.tvPhotoDate);
        TextView tvPhotoDescription = dialogView.findViewById(R.id.tvPhotoDescription);
        Button btnClosePhoto = dialogView.findViewById(R.id.btnClosePhoto);
        
        // Load image using Glide
        com.bumptech.glide.Glide.with(this)
                .load(imageUrl)
                .placeholder(R.color.light_gray)
                .error(R.color.light_gray)
                .into(ivPhotoFull);
        
        // Set date
        tvPhotoDate.setText(date != null ? date : "Walang petsa");
        
        // Set description
        if (description != null && !description.isEmpty()) {
            tvPhotoDescription.setText(description);
        } else {
            tvPhotoDescription.setText("Walang paglalarawan");
            tvPhotoDescription.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
        
        // Create dialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        
        // Set dialog to be full width
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
        
        // Close button listener
        btnClosePhoto.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        Log.d("TreatmentAppliedDetails", "onResume - refreshing data");
        loadTreatmentTimeline();
    }
}

