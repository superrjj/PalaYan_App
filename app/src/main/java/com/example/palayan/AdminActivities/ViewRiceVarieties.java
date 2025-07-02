package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Adapter.RiceVarietyAdapter;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ViewRiceVarieties extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<RiceVariety> varietyList;
    private RiceVarietyAdapter adapter;
    private DatabaseReference databaseVarieties;
    private ValueEventListener riceVarietyListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_rice_varieties);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddRiceVariety.class)));

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recycleViewer_RiceVarieties);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        varietyList = new ArrayList<>();
        adapter = new RiceVarietyAdapter(varietyList, this);
        recyclerView.setAdapter(adapter);

        //setup the database for retrieving data from firebase
        databaseVarieties = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachFirebaseListener(); // Always fetch fresh data when activity starts
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
                        if (variety != null) {
                            varietyList.add(variety);
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseData", "Error parsing variety", e);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ViewRiceVarieties.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        };

        databaseVarieties.addValueEventListener(riceVarietyListener);
    }
}
