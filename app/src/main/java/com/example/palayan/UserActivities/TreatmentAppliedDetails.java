package com.example.palayan.UserActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Adapter.PhotoTimelineAdapter;
import com.example.palayan.Adapter.TreatmentHistoryAdapter;
import com.example.palayan.Helper.PhotoTimelineModel;
import com.example.palayan.Helper.TreatmentHistoryModel;
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
    private PhotoTimelineAdapter photoTimelineAdapter;
    private TreatmentHistoryAdapter treatmentHistoryAdapter;
    private List<PhotoTimelineModel> photoTimelineList;
    private List<TreatmentHistoryModel> treatmentHistoryList;

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
        loadPhotoTimeline();
        loadTreatmentHistory();
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
        // Initialize photo timeline
        photoTimelineList = new ArrayList<>();
        photoTimelineAdapter = new PhotoTimelineAdapter(this, photoTimelineList);
        root.recyclerPhotoTimeline.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        root.recyclerPhotoTimeline.setAdapter(photoTimelineAdapter);

        photoTimelineAdapter.setOnPhotoClickListener((imageUrl, date) -> {
            // Simple full view dialog
            android.app.AlertDialog.Builder b = new android.app.AlertDialog.Builder(this);
            android.widget.ImageView iv = new android.widget.ImageView(this);
            iv.setAdjustViewBounds(true);
            iv.setBackgroundColor(getResources().getColor(android.R.color.black));
            int p = (int) (16 * getResources().getDisplayMetrics().density);
            iv.setPadding(p, p, p, p);
            com.bumptech.glide.Glide.with(this).load(imageUrl).into(iv);
            b.setView(iv);
            b.setPositiveButton("Close", (d, w) -> d.dismiss());
            b.show();
        });

        // Initialize treatment history
        treatmentHistoryList = new ArrayList<>();
        treatmentHistoryAdapter = new TreatmentHistoryAdapter(this, treatmentHistoryList);
        root.rvTreatmentHistory.setLayoutManager(new LinearLayoutManager(this));
        root.rvTreatmentHistory.setAdapter(treatmentHistoryAdapter);
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

    private void loadPhotoTimeline() {
        if (historyId != null && deviceId != null) {
            Log.d("TreatmentAppliedDetails", "Loading photo timeline for historyId: " + historyId + ", deviceId: " + deviceId);

            // Load photo timeline from treatment_notes collection
            // Based on Firebase structure: users/{deviceId}/treatment_notes
            db.collection("users")
                    .document(deviceId)
                    .collection("treatment_notes")
                    .whereEqualTo("documentId", historyId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("TreatmentAppliedDetails", "Photo timeline query completed. Documents: " + queryDocumentSnapshots.size());

                        photoTimelineList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String photoUrl = document.getString("photoUrl");
                            String dateApplied = document.getString("dateApplied");
                            Date timestamp = document.getDate("timestamp");

                            Log.d("TreatmentAppliedDetails", "Photo timeline item - photoUrl: " + photoUrl + ", dateApplied: " + dateApplied + ", timestamp: " + timestamp);

                            if (photoUrl != null && (dateApplied != null || timestamp != null)) {
                                String formattedDate = dateApplied != null ? dateApplied : formatDate(timestamp);
                                PhotoTimelineModel photo = new PhotoTimelineModel(photoUrl, formattedDate);
                                photoTimelineList.add(photo);
                            }
                        }

                        photoTimelineAdapter.notifyDataSetChanged();

                        if (photoTimelineList.isEmpty()) {
                            Log.d("TreatmentAppliedDetails", "No photos found in timeline");
                            Toast.makeText(this, "No photos found in timeline", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("TreatmentAppliedDetails", "Loaded " + photoTimelineList.size() + " photos in timeline");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TreatmentAppliedDetails", "Error loading photo timeline: " + e.getMessage());
                        Toast.makeText(this, "Error loading photo timeline: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.e("TreatmentAppliedDetails", "Cannot load photo timeline - missing historyId or deviceId");
        }
    }

    private void loadTreatmentHistory() {
        if (historyId != null && deviceId != null) {
            Log.d("TreatmentAppliedDetails", "Loading treatment history for historyId: " + historyId + ", deviceId: " + deviceId);

            // Load treatment history from treatment_notes collection
            // Based on Firebase structure: users/{deviceId}/treatment_notes
            db.collection("users")
                    .document(deviceId)
                    .collection("treatment_notes")
                    .whereEqualTo("documentId", historyId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        Log.d("TreatmentAppliedDetails", "Treatment history query completed. Documents: " + queryDocumentSnapshots.size());

                        treatmentHistoryList.clear();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String description = document.getString("description");
                            String dateApplied = document.getString("dateApplied");
                            Date timestamp = document.getDate("timestamp");

                            Log.d("TreatmentAppliedDetails", "Treatment history item - description: " + description + ", dateApplied: " + dateApplied + ", timestamp: " + timestamp);

                            if (description != null && (dateApplied != null || timestamp != null)) {
                                String formattedDate = dateApplied != null ? dateApplied : formatDate(timestamp);
                                TreatmentHistoryModel history = new TreatmentHistoryModel(formattedDate, description);
                                treatmentHistoryList.add(history);
                            }
                        }

                        // Sort by date string (MMM dd, yyyy) newest first
                        treatmentHistoryList.sort((a, b) -> {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                                Date dateA = sdf.parse(a.getDate());
                                Date dateB = sdf.parse(b.getDate());
                                return dateB.compareTo(dateA);
                            } catch (Exception e) {
                                return 0;
                            }
                        });

                        treatmentHistoryAdapter.notifyDataSetChanged();

                        if (treatmentHistoryList.isEmpty()) {
                            Log.d("TreatmentAppliedDetails", "No treatment history found");
                            Toast.makeText(this, "No treatment history found", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("TreatmentAppliedDetails", "Loaded " + treatmentHistoryList.size() + " treatment history items");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TreatmentAppliedDetails", "Error loading treatment history: " + e.getMessage());
                        Toast.makeText(this, "Error loading treatment history: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            Log.e("TreatmentAppliedDetails", "Cannot load treatment history - missing historyId or deviceId");
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

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning from other activities
        Log.d("TreatmentAppliedDetails", "onResume - refreshing data");
        loadPhotoTimeline();
        loadTreatmentHistory();
    }
}
