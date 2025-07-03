package com.example.palayan.TabFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.UserRiceVarietyAdapter;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.Helper.SearchQuery.SearchableFragment;

import com.example.palayan.databinding.FragmentTarlacBinding;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class TarlacFragment extends Fragment implements SearchableFragment {

    private FragmentTarlacBinding root;
    private List<RiceVariety> riceVarietyList;
    private List<RiceVariety> fullList;
    private UserRiceVarietyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentTarlacBinding.inflate(inflater, container, false);

        riceVarietyList = new ArrayList<>();
        fullList = new ArrayList<>();
        adapter = new UserRiceVarietyAdapter(riceVarietyList, getContext());
        root.rvTarlacRiceSeed.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvTarlacRiceSeed.setAdapter(adapter);

        loadTarlacData();
        root.tvNoData.setVisibility(View.GONE);


        return root.getRoot();
    }

    private void loadTarlacData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");
        ref.orderByChild("archived").equalTo(false)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        riceVarietyList.clear();
                        fullList.clear();

                        for (DataSnapshot data : snapshot.getChildren()) {
                            RiceVariety variety = data.getValue(RiceVariety.class);
                            if (variety != null && variety.location != null &&
                                    variety.location.toLowerCase().contains("tarlac")) {
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
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
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

        if (riceVarietyList.isEmpty()) {
            root.tvNoData.setVisibility(View.VISIBLE);
        } else {
            root.tvNoData.setVisibility(View.GONE);
        }
    }
}
