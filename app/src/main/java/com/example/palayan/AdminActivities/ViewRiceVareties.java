package com.example.palayan.AdminActivities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

public class ViewRiceVareties extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<RiceVariety> varietyList;
    private RiceVarietyAdapter adapter;
    private DatabaseReference databaseVarieties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_rice_vareties);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        FloatingActionButton fabAdd = findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(view -> {
            Intent intent = new Intent(ViewRiceVareties.this, AddRiceVariety.class);
            startActivity(intent);
        });

        ImageView ivBack = findViewById(R.id.iv_back);
        ivBack.setOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recycleViewer_RiceVarieties);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        varietyList = new ArrayList<>();
        adapter = new RiceVarietyAdapter(varietyList, this);
        recyclerView.setAdapter(adapter);

        databaseVarieties = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");

        databaseVarieties.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                varietyList.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    RiceVariety variety = postSnapshot.getValue(RiceVariety.class);
                    if (variety != null) {
                        varietyList.add(variety);
                    } else {
                        Log.w("FirebaseData", "Null variety for: " + postSnapshot.getKey());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ViewRiceVareties.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
