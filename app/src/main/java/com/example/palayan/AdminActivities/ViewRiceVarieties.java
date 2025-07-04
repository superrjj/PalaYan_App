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
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ViewRiceVarieties extends AppCompatActivity {

    private ActivityViewRiceVarietiesBinding root;
    private List<RiceVariety> varietyList;
    private List<RiceVariety> fullVarietyList;
    private AdminRiceVarietyAdapter adapter;
    private DatabaseReference databaseVarieties;
    private ValueEventListener riceVarietyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityViewRiceVarietiesBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        setSupportActionBar(root.toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        root.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddRiceVariety.class)));
        root.ivBack.setOnClickListener(v -> onBackPressed());

        root.recycleViewerRiceVarieties.setLayoutManager(new LinearLayoutManager(this));
        varietyList = new ArrayList<>();
        fullVarietyList = new ArrayList<>();
        adapter = new AdminRiceVarietyAdapter(varietyList, this);
        root.recycleViewerRiceVarieties.setAdapter(adapter);

        //setup the database for retrieving data from firebase
        databaseVarieties = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");

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
        attachFirebaseListener(); //always fetch fresh data when activity starts to stay realtime updated
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (databaseVarieties != null && riceVarietyListener != null) {
            databaseVarieties.removeEventListener(riceVarietyListener);
        }
    }

    private void attachFirebaseListener() {
        riceVarietyListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                varietyList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    try {
                        RiceVariety variety = postSnapshot.getValue(RiceVariety.class);
                        if (variety != null && !variety.archived) {
                            varietyList.add(variety);
                            fullVarietyList.add(variety);
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseData", "Error parsing variety", e);
                    }
                }
                adapter.notifyDataSetChanged();
                root.tvNoData.setVisibility(varietyList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ViewRiceVarieties.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        };

        databaseVarieties.addValueEventListener(riceVarietyListener);
    }

    private void filterList(String query) {
        List<RiceVariety> filteredList = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (RiceVariety item : fullVarietyList) {
            if ((item.varietyName != null && item.varietyName.toLowerCase().contains(lowerQuery)) ||
                    (item.location != null && item.location.toLowerCase().contains(lowerQuery)) ||
                    (item.yearRelease != null && item.yearRelease.toLowerCase().contains(lowerQuery))) {
                filteredList.add(item);
            }
            varietyList.clear();
            varietyList.addAll(filteredList);
            adapter.notifyDataSetChanged();

            root.tvNoData.setVisibility(varietyList.isEmpty() ? View.VISIBLE : View.GONE);

        }
    }
}
