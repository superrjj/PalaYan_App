package com.example.palayan.BottomFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.palayan.Helper.SearchQuery.OnFilterAppliedListener;
import com.example.palayan.R;
import com.example.palayan.TabFragment.AllFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RiceFilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private AutoCompleteTextView actLocation, actYear, actSeason, actMethod, actEnvironment;
    private Button btnApplyFilter;

    private OnFilterAppliedListener filterAppliedListener;
    
    private FirebaseFirestore firestore;

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.filterAppliedListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_dialog_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        actLocation = view.findViewById(R.id.act_location);
        actYear = view.findViewById(R.id.act_year);
        actSeason = view.findViewById(R.id.act_season);
        actMethod = view.findViewById(R.id.act_plant_method);
        actEnvironment = view.findViewById(R.id.act_environment);
        btnApplyFilter = view.findViewById(R.id.btnFilter);

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance();

        // Load data from Firebase
        loadFilterDataFromFirebase();

        btnApplyFilter.setOnClickListener(v -> {
            String location = actLocation.getText().toString().trim();
            String year = actYear.getText().toString().trim();
            String season = actSeason.getText().toString().trim();
            String method = actMethod.getText().toString().trim();
            String environment = actEnvironment.getText().toString().trim();

            if (getTargetFragment() instanceof AllFragment) {
                ((AllFragment) getTargetFragment()).filterRiceVarietiesFirestore(
                        location.isEmpty() ? null : location,
                        year.isEmpty() ? null : year,
                        season.isEmpty() ? null : season,
                        method.isEmpty() ? null : method,
                        environment.isEmpty() ? null : environment
                );
            }

            dismiss();
        });
    }

    private void loadFilterDataFromFirebase() {
        // Load enums from maintenance/rice_varieties_enums
        firestore.collection("maintenance")
                .document("rice_varieties_enums")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Load environments
                        if (documentSnapshot.contains("environments")) {
                            List<String> environments = (List<String>) documentSnapshot.get("environments");
                            setupDropdownFromList(actEnvironment, environments);
                        }
                        
                        // Load seasons
                        if (documentSnapshot.contains("seasons")) {
                            List<String> seasons = (List<String>) documentSnapshot.get("seasons");
                            setupDropdownFromList(actSeason, seasons);
                        }
                        
                        // Load planting methods
                        if (documentSnapshot.contains("plantingMethods")) {
                            List<String> plantingMethods = (List<String>) documentSnapshot.get("plantingMethods");
                            setupDropdownFromList(actMethod, plantingMethods);
                        }
                        
                        // Load years
                        if (documentSnapshot.contains("yearReleases")) {
                            List<String> years = (List<String>) documentSnapshot.get("yearReleases");
                            setupDropdownFromList(actYear, years);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FilterBottomSheet", "Failed to load enums: " + e.getMessage());
                });
        
        // Load locations from rice_seed_varieties collection
        loadLocationsFromRiceVarieties();
    }
    
    private void loadLocationsFromRiceVarieties() {
        Set<String> uniqueLocations = new LinkedHashSet<>();
        
        firestore.collection("rice_seed_varieties")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        // Check if variety is deleted
                        Boolean isDeleted = document.getBoolean("isDeleted");
                        if (isDeleted != null && isDeleted) {
                            continue; // Skip deleted varieties
                        }
                        
                        // Only add location if not deleted
                        if (document.contains("location") && document.get("location") != null) {
                            String location = document.getString("location");
                            if (location != null && !location.isEmpty()) {
                                uniqueLocations.add(location);
                            }
                        }
                    }
                    
                    // Convert to list and sort
                    List<String> locationList = new ArrayList<>(uniqueLocations);
                    java.util.Collections.sort(locationList);
                    
                    setupDropdownFromList(actLocation, locationList);
                })
                .addOnFailureListener(e -> {
                    Log.e("FilterBottomSheet", "Failed to load locations: " + e.getMessage());
                });
    }
    
    private void setupDropdownFromList(AutoCompleteTextView actView, List<String> data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                data
        );
        actView.setAdapter(adapter);
    }
    
    private void setupDropdown(AutoCompleteTextView actView, int arrayRes) {
        String[] array = getResources().getStringArray(arrayRes);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, array);
        actView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            dialog.getBehavior().setPeekHeight(900);
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(R.drawable.dialog_background);
            }
        }
    }
}
