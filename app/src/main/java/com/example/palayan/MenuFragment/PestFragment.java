package com.example.palayan.MenuFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.UserPestAdapter;
import com.example.palayan.Helper.Pest;
import com.example.palayan.databinding.FragmentPestBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PestFragment extends Fragment {

    private FragmentPestBinding root;
    private List<Pest> pestList;
    private FirebaseFirestore firestore;
    private UserPestAdapter adapter;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentPestBinding.inflate(inflater, container, false);

        firestore = FirebaseFirestore.getInstance();
        pestList = new ArrayList<>();
        adapter = new UserPestAdapter(pestList, getContext());

        root.rvViewPestUser.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvViewPestUser.setAdapter(adapter);

        return root.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        attachListener();
    }

    private void attachListener() {
        listenerRegistration = firestore.collection("rice_local_pests")
                .whereEqualTo("isDeleted", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Error loading pests: " + e.getMessage());
                        return;
                    }

                    pestList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Pest pest = doc.toObject(Pest.class);
                        if (pest != null) {
                            // CORRECTED: Set the document ID
                            pest.setDocumentId(doc.getId());
                            pestList.add(pest);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    //kapag walang pest data
                    if (root.tvNoData != null) {
                        root.tvNoData.setVisibility(pestList.isEmpty() ? View.VISIBLE : View.GONE);
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
    public void onDestroyView() {
        super.onDestroyView();
        root = null;
    }
}