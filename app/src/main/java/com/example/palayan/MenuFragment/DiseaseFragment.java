package com.example.palayan.MenuFragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.palayan.Adapter.UserDiseaseAdapter;
import com.example.palayan.Helper.Disease;
import com.example.palayan.databinding.FragmentDiseaseBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DiseaseFragment extends Fragment {

    private FragmentDiseaseBinding root;
    private List<Disease> diseaseList;
    private FirebaseFirestore firestore;
    private UserDiseaseAdapter adapter;
    private ListenerRegistration listenerRegistration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = FragmentDiseaseBinding.inflate(inflater, container, false);

        firestore = FirebaseFirestore.getInstance();
        diseaseList = new ArrayList<>();
        adapter = new UserDiseaseAdapter(diseaseList, getContext());

        root.rvViewDiseaseUser.setLayoutManager(new LinearLayoutManager(getContext()));
        root.rvViewDiseaseUser.setAdapter(adapter);

        return root.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        attachListener();
    }

    private void attachListener() {
        listenerRegistration = firestore.collection("rice_local_diseases")
                .whereEqualTo("isDeleted", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }


                    diseaseList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {

                        Disease disease = doc.toObject(Disease.class);
                        if (disease != null) {
                            disease.setDocumentId(doc.getId());
                            diseaseList.add(disease);
                        } else {

                        }
                    }
                    adapter.notifyDataSetChanged();

                    //kapag walang disease data
                    if (root.tvNoData != null) {
                        root.tvNoData.setVisibility(diseaseList.isEmpty() ? View.VISIBLE : View.GONE);
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