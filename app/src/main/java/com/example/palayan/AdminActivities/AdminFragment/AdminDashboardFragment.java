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

import com.example.palayan.AdminActivities.ViewRiceVarieties;
import com.example.palayan.databinding.FragmentAdminDashboardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding root;
    private DatabaseReference riceVarietyRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return root.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        riceVarietyRef = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");

        //realtime listener to count rice seeds that are not archived
        riceVarietyRef.orderByChild("archived").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        long count = snapshot.getChildrenCount();
                        root.tvRiceSeedCount.setText(String.valueOf(count));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Failed to load count", Toast.LENGTH_SHORT).show();
                    }
                });

        //on card click, navigate to RiceVariety list
        root.cvRiceVarieties.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ViewRiceVarieties.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}
