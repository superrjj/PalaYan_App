package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.AdminPestAdapter;
import com.example.palayan.Helper.Pest;
import com.example.palayan.databinding.ActivityViewPestBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewPest extends AppCompatActivity {

    private ActivityViewPestBinding root;
    private AdminPestAdapter pestAdapter;
    private final List<Pest> pestList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private ListenerRegistration pestListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewPestBinding.inflate(getLayoutInflater());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(root.getRoot());

        firestore = FirebaseFirestore.getInstance();

        root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddPest.class)));
        root.ivBack.setOnClickListener(v -> onBackPressed());

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        pestAdapter = new AdminPestAdapter(pestList, this);
        root.recycleViewerPest.setLayoutManager(new LinearLayoutManager(this));
        root.recycleViewerPest.setAdapter(pestAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachPestListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (pestListener != null) {
            pestListener.remove();
        }
    }

    private void attachPestListener() {
        pestListener = firestore.collection("pests")
                .whereEqualTo("archived", false)
                .addSnapshotListener((QuerySnapshot snapshots, com.google.firebase.firestore.FirebaseFirestoreException e) -> {
                    if (e != null) return;

                    pestList.clear();

                    if (snapshots != null) {
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Pest pest = doc.toObject(Pest.class);
                            if (pest != null) {
                                pestList.add(pest);
                            }
                        }
                    }

                    pestAdapter.notifyDataSetChanged();
                    root.tvNoData.setVisibility(pestList.isEmpty() ? View.VISIBLE : View.GONE);
                });

    }
}
