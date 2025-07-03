package com.example.palayan.TabFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.UserRiceVarietyAdapter;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.Helper.SearchQuery.SearchableFragment;
import com.example.palayan.databinding.FragmentAllBinding;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class AllFragment extends Fragment implements SearchableFragment {

    private FragmentAllBinding root;
    private List<RiceVariety> riceVarietyList;
    private List<RiceVariety> fullList;
    private UserRiceVarietyAdapter adapter;
    private DatabaseReference databaseRef;
    private ValueEventListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = FragmentAllBinding.inflate(inflater, container, false);

        riceVarietyList = new ArrayList<>();
        fullList = new ArrayList<>();

        adapter = new UserRiceVarietyAdapter(riceVarietyList, getContext());
        root.rvAllRiceSeed.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvAllRiceSeed.setAdapter(adapter);

        databaseRef = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");

        return root.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        attachListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listener != null) {
            databaseRef.removeEventListener(listener);
        }
    }

    private void attachListener() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                riceVarietyList.clear();
                fullList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    RiceVariety variety = data.getValue(RiceVariety.class);
                    if (variety != null && !Boolean.TRUE.equals(variety.archived)) {
                        riceVarietyList.add(variety);
                        fullList.add(variety);
                    }
                }

                adapter.notifyDataSetChanged();
                if (root != null) {
                    root.tvNoData.setVisibility(riceVarietyList.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error: " + error.getMessage());
            }
        };

        databaseRef.addValueEventListener(listener);
    }

    @Override
    public void filter(String query) {
        List<RiceVariety> filteredList = new ArrayList<>();
        String lower = query.toLowerCase();

        for (RiceVariety item : fullList) {
            if ((item.varietyName != null && item.varietyName.toLowerCase().contains(lower)) ||
                    (item.location != null && item.location.toLowerCase().contains(lower)) ||
                    (item.yearRelease != null && item.yearRelease.toLowerCase().contains(lower))) {
                filteredList.add(item);
            }
        }

        riceVarietyList.clear();
        riceVarietyList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (root != null) {
            root.tvNoData.setVisibility(riceVarietyList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}
