package com.example.palayan.TabFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.palayan.Adapter.RiceVarietyAdapter;
import com.example.palayan.Adapter.UserRiceVarietyAdapter;
import com.example.palayan.Helper.RiceVariety;
import com.example.palayan.R;
import com.example.palayan.databinding.FragmentAllBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class AllFragment extends Fragment {

    private FragmentAllBinding root;
    private List<RiceVariety> riceVarietyList;
    private UserRiceVarietyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentAllBinding.inflate(inflater, container, false);

        riceVarietyList = new ArrayList<>();
        adapter = new UserRiceVarietyAdapter(riceVarietyList, getContext());


        root.rvAllRiceSeed.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvAllRiceSeed.setAdapter(adapter);

        loadAllRiceVarieties();

        return root.getRoot();
    }

    private void loadAllRiceVarieties() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("rice_seed_varieties");
        ref.orderByChild("archived").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        riceVarietyList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            RiceVariety variety = data.getValue(RiceVariety.class);
                            if (variety != null) {
                                riceVarietyList.add(variety);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseError", error.getMessage());
                    }
                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }

}