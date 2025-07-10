package com.example.palayan.TabFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.UserRiceVarietyAdapter;
import com.example.palayan.Helper.DeviceUtils;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.databinding.FragmentFavoritesBinding;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding root;
    private List<RiceVariety> favoriteList;
    private UserRiceVarietyAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = FragmentFavoritesBinding.inflate(inflater, container, false);

        favoriteList = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();

        adapter = new UserRiceVarietyAdapter(favoriteList, getContext(), null, true);
        root.rvFavoriteRiceSeed.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvFavoriteRiceSeed.setAdapter(adapter);

        loadFavorites();

        root.tvNoData.setVisibility(View.GONE);

        return root.getRoot();
    }

    private void loadFavorites() {
        String deviceId = DeviceUtils.getDeviceId(requireContext());

        listenerRegistration = firestore.collection("rice_seed_favorites")
                .whereEqualTo("deviceId", deviceId)
                .addSnapshotListener((favoritesSnapshot, e) -> {
                    if (e != null) return;

                    favoriteList.clear();

                    if (favoritesSnapshot.isEmpty()) {
                        adapter.notifyDataSetChanged();
                        root.tvNoData.setVisibility(View.VISIBLE);
                        return;
                    }

                    List<String> favoriteIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : favoritesSnapshot) {
                        String riceId = doc.getString("rice_seed_id");
                        if (riceId != null) {
                            favoriteIds.add(riceId);
                        }
                    }

                    if (!favoriteIds.isEmpty()) {
                        firestore.collection("rice_seed_varieties")
                                .whereIn("rice_seed_id", favoriteIds)
                                .get()
                                .addOnSuccessListener(varietiesSnapshot -> {
                                    favoriteList.clear();
                                    for (QueryDocumentSnapshot doc : varietiesSnapshot) {
                                        RiceVariety variety = doc.toObject(RiceVariety.class);
                                        favoriteList.add(variety);
                                    }

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
}
