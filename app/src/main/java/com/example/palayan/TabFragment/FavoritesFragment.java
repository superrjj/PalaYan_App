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
import com.example.palayan.databinding.FragmentFavoritesBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FavoritesFragment extends Fragment implements SearchableFragment {

    private FragmentFavoritesBinding root;
    private List<RiceVariety> favoriteList;
    private List<RiceVariety> fullList;
    private UserRiceVarietyAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;
    private Set<String> favoriteIds = new HashSet<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = FragmentFavoritesBinding.inflate(inflater, container, false);

        favoriteList = new ArrayList<>();
        fullList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        // Initialize adapter with empty list
        adapter = new UserRiceVarietyAdapter(favoriteList, getContext(), favoriteIds, true);
        root.rvFavoriteRiceSeed.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvFavoriteRiceSeed.setAdapter(adapter);

        root.tvNoData.setVisibility(View.GONE);

        return root.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        loadFavorites();
    }

    private void loadFavorites() {
        String deviceId = DeviceUtils.getDeviceId(requireContext());

        listenerRegistration = firestore.collection("rice_seed_favorites")
                .whereEqualTo("deviceId", deviceId)
                .addSnapshotListener((favoritesSnapshot, e) -> {
                    if (e != null) return;

                    favoriteList.clear();
                    fullList.clear();

                    if (favoritesSnapshot.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        root.tvNoData.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> favoriteNames = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : favoritesSnapshot) {
                        String riceId = doc.getString("rice_seed_id");
                        if (riceId != null) {
                            favoriteNames.add(riceId);
                        }
                    }

                    if (!favoriteNames.isEmpty()) {
                        // Query rice varieties by variety name (since rice_seed_id is actually variety name)
                        firestore.collection("rice_seed_varieties")
                                .whereEqualTo("isDeleted", false)
                                .get()
                                .addOnSuccessListener(varietiesSnapshot -> {
                                    favoriteList.clear();
                                    fullList.clear();
                                    Map<String, String> documentIdMap = new HashMap<>();

                                    for (QueryDocumentSnapshot doc : varietiesSnapshot) {
                                        RiceVariety variety = doc.toObject(RiceVariety.class);
                                        if (variety != null && favoriteNames.contains(variety.varietyName)) {
                                            favoriteList.add(variety);
                                            fullList.add(variety);

                                            // Map variety name to document ID
                                            String key = variety.varietyName != null ? variety.varietyName : "variety_" + doc.getId();
                                            documentIdMap.put(key, doc.getId());
                                        }
                                    }

                                    adapter.setDocumentIdMap(documentIdMap);
                                    adapter.notifyDataSetChanged();
                                    root.tvNoData.setVisibility(favoriteList.isEmpty() ? View.VISIBLE : View.GONE);
                                });
                    } else {
                        favoriteList.clear();
                        adapter.notifyDataSetChanged();
                        root.tvNoData.setVisibility(View.VISIBLE);
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

        favoriteList.clear();
        favoriteList.addAll(filteredList);
        adapter.notifyDataSetChanged();

        if (root != null) {
            root.tvNoData.setVisibility(favoriteList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}