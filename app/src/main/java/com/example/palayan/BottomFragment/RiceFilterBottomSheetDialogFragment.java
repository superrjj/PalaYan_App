package com.example.palayan.BottomFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.palayan.Helper.OnFilterAppliedListener;
import com.example.palayan.R;
import com.example.palayan.TabFragment.AllFragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RiceFilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

    private AutoCompleteTextView actLocation, actYear, actSeason, actMethod, actEnvironment;
    private Button btnApplyFilter;

    private OnFilterAppliedListener filterAppliedListener;

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

        setupDropdown(actLocation, R.array.region_array);
        setupDropdown(actYear, R.array.year_array);
        setupDropdown(actSeason, R.array.seasons_array);
        setupDropdown(actMethod, R.array.planting_methods_array);
        setupDropdown(actEnvironment, R.array.environment_array);

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
            dialog.getBehavior().setPeekHeight(600);
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(R.drawable.dialog_background);
            }
        }
    }
}
