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

import com.example.palayan.AdminActivities.ViewAccounts;
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
    private ListenerRegistration riceVarietyListener, accountListener;


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

        // Get the role from the bundle
        String userRole = getArguments() != null ? getArguments().getString("userRole") : "";

        //enable/disable based on role
        if (userRole.equals("Data Manager")) {
            root.cvAccounts.setEnabled(false); // disable View Accounts card
            root.cvAccounts.setAlpha(0.5f);    // visually gray it out (optional)
        } else {
            root.cvAccounts.setEnabled(true);
        }

        // Firestore realtime listener to count rice seeds
        riceVarietyListener = firestore.collection("rice_seed_varieties")
                .whereEqualTo("archived", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Failed to load count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        long count = snapshots.size();
                        root.tvRiceSeedCount.setText(String.valueOf(count));
                    }
                });

        accountListener = firestore.collection("accounts")
                .whereEqualTo("archived", false)
                .addSnapshotListener((snapshots, e) ->{
                    if (e != null) {
                        Toast.makeText(getContext(), "Failed to load count: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        long count = snapshots.size();
                        root.tvAccountCounts.setText(String.valueOf(count));
                    }
                });


        root.cvRiceVarieties.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ViewRiceVarieties.class));
        });

        root.cvPest.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ViewPest.class));
        });

        root.cvAccounts.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ViewAccounts.class));
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
