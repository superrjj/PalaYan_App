package com.example.palayan.BottomFragment;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import com.example.palayan.Adapter.HistoryAdapter;
import com.example.palayan.Helper.HistoryResult;
import com.example.palayan.R;
import com.example.palayan.UserActivities.CameraScanner;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private CardView btnCamera;
    private RecyclerView recycleViewerHistoryResult;
    private TextView tvNoData;

    private List<HistoryResult> historyList;
    private List<HistoryResult> predictionItems;
    private List<HistoryResult> treatmentItems;
    private HistoryAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration predictionsListener;
    private ListenerRegistration treatmentListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        btnCamera = view.findViewById(R.id.btnCamera);
        recycleViewerHistoryResult = view.findViewById(R.id.recycleViewer_HistoryResult);
        tvNoData = view.findViewById(R.id.tvNoData);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Initialize history list and adapter
        historyList = new ArrayList<>();
        predictionItems = new ArrayList<>();
        treatmentItems = new ArrayList<>();
        adapter = new HistoryAdapter(historyList, getContext());

        // Setup RecyclerView
        recycleViewerHistoryResult.setLayoutManager(new LinearLayoutManager(getContext()));
        recycleViewerHistoryResult.setAdapter(adapter);

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CameraScanner.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadHistoryData();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove listeners to prevent memory leaks
        if (predictionsListener != null) {
            predictionsListener.remove();
        }
        if (treatmentListener != null) {
            treatmentListener.remove();
        }
    }

    private String getDeviceId() {
        String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return androidId;
    }

    private void loadHistoryData() {
        String deviceId = getDeviceId();
        // Clear existing data
        historyList.clear();
        adapter.notifyDataSetChanged();

        // Remove previous listeners
        if (predictionsListener != null) {
            predictionsListener.remove();
            predictionsListener = null;
        }
        if (treatmentListener != null) {
            treatmentListener.remove();
            treatmentListener = null;
        }

        // Reset source lists
        predictionItems.clear();
        treatmentItems.clear();

        // Listen to predictions_result
        predictionsListener = firestore.collection("users")
                .document(deviceId)
                .collection("predictions_result")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("HomeFragment", "Firestore error: " + e.getMessage());
                        return;
                    }

                    predictionItems.clear();

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            HistoryResult history = doc.toObject(HistoryResult.class);
                            if (history != null) {
                                if (history.getDateApplied() == null || history.getDateApplied().isEmpty()) {
                                    history.setDocumentId(doc.getId());
                                    history.setUserId(deviceId);
                                    history.setExplicitType("prediction");
                                    predictionItems.add(history);
                                }
                            }
                        }
                    }

                    refreshMergedHistory();
                });

        // Listen to treatment_notes
        treatmentListener = firestore.collection("users")
                .document(deviceId)
                .collection("treatment_notes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("HomeFragment", "Firestore error (treatment): " + e.getMessage());
                        return;
                    }

                    treatmentItems.clear();

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            HistoryResult history = doc.toObject(HistoryResult.class);
                            if (history != null) {
                                history.setDocumentId(doc.getString("documentId") != null ? doc.getString("documentId") : doc.getId());
                                history.setUserId(deviceId);
                                history.setExplicitType("treatment");
                                treatmentItems.add(history);
                            }
                        }
                    }

                    refreshMergedHistory();
                });
    }

    private void refreshMergedHistory() {
        historyList.clear();
        historyList.addAll(predictionItems);
        historyList.addAll(treatmentItems);

        // Sort by timestamp desc (nulls last)
        historyList.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        adapter.notifyDataSetChanged();
        tvNoData.setVisibility(historyList.isEmpty() ? View.VISIBLE : View.GONE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            }
        }
    }
}