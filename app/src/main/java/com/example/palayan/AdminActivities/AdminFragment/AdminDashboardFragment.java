package com.example.palayan.AdminActivities.AdminFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.palayan.AdminActivities.ViewPest;
import com.example.palayan.AdminActivities.ViewRiceVarieties;
import com.example.palayan.databinding.FragmentAdminDashboardBinding;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding root;
    private FirebaseFirestore firestore;
    private ListenerRegistration riceVarietyListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return root.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();

        // Firestore realtime listener to count non-archived rice seed varieties
        riceVarietyListener = firestore.collection("rice_seed_varieties")
                .whereEqualTo("archived", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(getContext(), "Failed to load count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots != null) {
                            long count = snapshots.size();
                            root.tvRiceSeedCount.setText(String.valueOf(count));
                        }
                    }
                });

        // On card click, navigate to RiceVariety list
        root.cvRiceVarieties.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ViewRiceVarieties.class);
            startActivity(intent);
        });

        root.cvPest.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ViewPest.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // detach listener to prevent memory leak
        if (riceVarietyListener != null) {
            riceVarietyListener.remove();
        }
        root = null;
    }
}
