package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.AdminRiceVarietyAdapter;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.databinding.ActivityViewRiceVarietiesBinding;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class ViewRiceVarieties extends AppCompatActivity {

    private ActivityViewRiceVarietiesBinding root;
    private List<RiceVariety> varietyList;
    private List<RiceVariety> fullVarietyList;
    private AdminRiceVarietyAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration riceVarietyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewRiceVarietiesBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        setSupportActionBar(root.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //firestore instance
        firestore = FirebaseFirestore.getInstance();

        //go to the add section
        root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddRiceVariety.class)));

        // Back Button
        root.ivBack.setOnClickListener(v -> onBackPressed());

        //recyclerView for the list seed layout
        root.recycleViewerRiceVarieties.setLayoutManager(new LinearLayoutManager(this));
        varietyList = new ArrayList<>();
        fullVarietyList = new ArrayList<>();
        adapter = new AdminRiceVarietyAdapter(varietyList, this);
        root.recycleViewerRiceVarieties.setAdapter(adapter);

        //search view
        root.svSearchBar.setQueryHint("Search rice variety...");
        root.svSearchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                filterList(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterList(s);
                return true;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachFirestoreListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (riceVarietyListener != null) {
            riceVarietyListener.remove();
        }
    }

    // Firestore realtime listener setup
    private void attachFirestoreListener() {
        riceVarietyListener = firestore.collection("rice_seed_varieties")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable com.google.firebase.firestore.FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ViewRiceVarieties.this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        varietyList.clear();
                        fullVarietyList.clear();

                        if (snapshots != null) {
                            for (QueryDocumentSnapshot doc : snapshots) {
                                try {
                                    RiceVariety variety = doc.toObject(RiceVariety.class);
                                    if (variety != null && !variety.archived) {
                                        varietyList.add(variety);
                                        fullVarietyList.add(variety);
                                    }
                                } catch (Exception ex) {
                                    Log.e("FirestoreData", "Error parsing variety", ex);
                                }
                            }
                        }

                        adapter.notifyDataSetChanged();
                        root.tvNoData.setVisibility(varietyList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    // Filter list based on query string
    private void filterList(String query) {
        List<RiceVariety> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (RiceVariety item : fullVarietyList) {
            if ((item.varietyName != null && item.varietyName.toLowerCase().contains(lowerQuery)) ||
                    (item.location != null && item.location.toLowerCase().contains(lowerQuery)) ||
                    (item.yearRelease != null && item.yearRelease.toLowerCase().contains(lowerQuery))) {
                filteredList.add(item);
            }
        }

        varietyList.clear();
        varietyList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        root.tvNoData.setVisibility(varietyList.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
