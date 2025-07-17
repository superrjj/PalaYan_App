package com.example.palayan.AdminActivities.AdminFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.palayan.AdminActivities.AdminDashboard;
import com.example.palayan.AdminActivities.ViewAccounts;
import com.example.palayan.AdminActivities.ViewPest;
import com.example.palayan.AdminActivities.ViewRiceVarieties;
import com.example.palayan.databinding.FragmentAdminDashboardBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding root;
    private FirebaseFirestore firestore;
    private ListenerRegistration riceVarietyListener, accountListener, pestListener;

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

        String userRole = getArguments() != null ? getArguments().getString("userRole") : "";
        int userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;

        if (userRole.equals("Data Manager")) {
            root.cvAccounts.setEnabled(false);
            root.cvAccounts.setAlpha(0.5f);
        } else {
            root.cvAccounts.setEnabled(true);
        }

        //counter for rice
        riceVarietyListener = firestore.collection("rice_seed_varieties")
                .whereEqualTo("archived", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || root == null) return;
                    long count = (snapshots != null) ? snapshots.size() : 0;
                    root.tvRiceSeedCount.setText(String.valueOf(count));
                });

        //counter for accounts
        pestListener = firestore.collection("pests")
                .whereEqualTo("archived", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || root == null) return;
                    long count = (snapshots != null) ? snapshots.size() : 0;
                    root.tvPestCount.setText(String.valueOf(count));
                });

        //counter for accounts
        accountListener = firestore.collection("accounts")
                .whereEqualTo("archived", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || root == null) return;
                    long count = (snapshots != null) ? snapshots.size() : 0;
                    root.tvAccountCounts.setText(String.valueOf(count));
                });

        //intent for the specific card view
        root.cvRiceVarieties.setOnClickListener(v -> {
            ((AdminDashboard) requireActivity()).setUserLeavingFalse();
            startActivity(new Intent(getActivity(), ViewRiceVarieties.class));
        });

        root.cvPest.setOnClickListener(v -> {
            ((AdminDashboard) requireActivity()).setUserLeavingFalse();
            startActivity(new Intent(getActivity(), ViewPest.class));
        });

        root.cvAccounts.setOnClickListener(v -> {
            ((AdminDashboard) requireActivity()).setUserLeavingFalse();
            Intent intent = new Intent(getActivity(), ViewAccounts.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
    }

    //for remove the previous listener
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (riceVarietyListener != null) {
            riceVarietyListener.remove();
            riceVarietyListener = null;
        }
        if (accountListener != null) {
            accountListener.remove();
            accountListener = null;
        }

        root = null;
    }
}
