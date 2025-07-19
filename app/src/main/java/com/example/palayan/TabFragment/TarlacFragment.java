package com.example.palayan.TabFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.UserRiceVarietyAdapter;
import com.example.palayan.Helper.AppHelper.DeviceUtils;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.Helper.SearchQuery.SearchableFragment;
import com.example.palayan.databinding.FragmentTarlacBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TarlacFragment extends Fragment implements SearchableFragment {

    private FragmentTarlacBinding root;
    private List<RiceVariety> riceVarietyList;
    private List<RiceVariety> fullList;
    private UserRiceVarietyAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;
    private Set<String> favoriteIds = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = FragmentTarlacBinding.inflate(inflater, container, false);

        riceVarietyList = new ArrayList<>();
        fullList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Adapter initialized here with empty list
        adapter = new UserRiceVarietyAdapter(riceVarietyList, getContext(), favoriteIds, false);
        root.rvTarlacRiceSeed.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvTarlacRiceSeed.setAdapter(adapter);

        return root.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavoritesAndTarlacData();
    }

    private void loadFavoritesAndTarlacData() {
        String deviceId = DeviceUtils.getDeviceId(requireContext());
        firestore.collection("rice_seed_favorites")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    favoriteIds.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        favoriteIds.add(doc.getString("rice_seed_id"));
                    }
                    loadTarlacData();
                });
    }

    private void loadTarlacData() {
        listenerRegistration = firestore.collection("rice_seed_varieties")
                .whereEqualTo("archived", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    riceVarietyList.clear();
                    fullList.clear();

                    for (QueryDocumentSnapshot doc : snapshots) {
                        RiceVariety variety = doc.toObject(RiceVariety.class);
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
                });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    @Override
    public void filter(String query) {
        if (adapter == null || fullList == null) return;

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
}
