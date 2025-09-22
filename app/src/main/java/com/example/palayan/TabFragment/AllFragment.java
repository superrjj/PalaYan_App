package com.example.palayan.TabFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.UserRiceVarietyAdapter;
import com.example.palayan.Helper.AppHelper.DeviceUtils;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.Helper.SearchQuery.SearchableFragment;
import com.example.palayan.databinding.FragmentAllBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AllFragment extends Fragment implements SearchableFragment {

    private FragmentAllBinding root;
    private List<RiceVariety> riceVarietyList;
    private List<RiceVariety> fullList;
    private UserRiceVarietyAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;
    private Set<String> favoriteIds = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = FragmentAllBinding.inflate(inflater, container, false);

        riceVarietyList = new ArrayList<>();
        fullList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        root.rvAllRiceSeed.setLayoutManager(new LinearLayoutManager(getContext()));

        return root.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavoritesAndRiceVarieties();
    }

    private void loadFavoritesAndRiceVarieties() {
        String deviceId = DeviceUtils.getDeviceId(requireContext());
        firestore.collection("rice_seed_favorites")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    favoriteIds.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        favoriteIds.add(doc.getString("rice_seed_id"));
                    }
                    attachListener();
                });
    }

    private void attachListener() {
        listenerRegistration = firestore.collection("rice_seed_varieties")
                .whereEqualTo("isDeleted", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error: " + e.getMessage());
                        return;
                    }

                    riceVarietyList.clear();
                    fullList.clear();
                    Map<String, String> documentIdMap = new HashMap<>();

                    for (QueryDocumentSnapshot document : snapshots) {
                        RiceVariety variety = document.toObject(RiceVariety.class);
                        if (variety != null) {
                            riceVarietyList.add(variety);
                            fullList.add(variety);

                            // Map variety name to document ID
                            String key = variety.varietyName != null ? variety.varietyName : "variety_" + document.getId();
                            documentIdMap.put(key, document.getId());

                            Log.d("AllFragment", "Mapped variety: " + key + " to document ID: " + document.getId());
                        }
                    }

                    if (root != null) {
                        adapter = new UserRiceVarietyAdapter(riceVarietyList, getContext(), favoriteIds, false);
                        adapter.setDocumentIdMap(documentIdMap);
                        root.rvAllRiceSeed.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
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

    //Firestore filtering function
    public void filterRiceVarietiesFirestore(String location, String year, String season, String plantingMethod, String environment) {
        firestore.collection("rice_seed_varieties")
                .whereEqualTo("isDeleted", false)
                .get()
                .addOnSuccessListener(snapshot -> {
                    riceVarietyList.clear();
                    for (QueryDocumentSnapshot document : snapshot) {
                        RiceVariety variety = document.toObject(RiceVariety.class);

                        boolean matches = true;

                        if (location != null && !location.isEmpty() && !location.equals(variety.location)) {
                            matches = false;
                        }

                        if (year != null && !year.isEmpty() && !year.equals(variety.yearRelease)) {
                            matches = false;
                        }

                        if (season != null && !season.isEmpty() && (variety.season == null || !variety.season.contains(season))) {
                            matches = false;
                        }

                        if (plantingMethod != null && !plantingMethod.isEmpty() && (variety.plantingMethod == null || !variety.plantingMethod.contains(plantingMethod))) {
                            matches = false;
                        }

                        if (environment != null && !environment.isEmpty() && (variety.environment == null || !variety.environment.contains(environment))) {
                            matches = false;
                        }

                        if (matches) {
                            riceVarietyList.add(variety);
                        }
                    }

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }

                    if (root != null) {
                        root.tvNoData.setVisibility(riceVarietyList.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }).addOnFailureListener(e -> Log.e("Firestore", "Filter Error: " + e.getMessage()));
    }

    @Override
    public void filter(String query) {
        if (adapter == null) {
            return;
        }

        if (query == null || query.trim().isEmpty()) {
            // No query — restore full list
            riceVarietyList.clear();
            riceVarietyList.addAll(fullList);
            adapter.notifyDataSetChanged();

            if (root != null) {
                root.tvNoData.setVisibility(riceVarietyList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            return;
        }

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