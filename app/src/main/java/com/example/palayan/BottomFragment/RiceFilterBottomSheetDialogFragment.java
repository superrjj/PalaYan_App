package com.example.palayan.BottomFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.palayan.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class RiceFilterBottomSheetDialogFragment extends BottomSheetDialogFragment {

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

        // setup string array adapter
        String[] environments = getResources().getStringArray(R.array.environment_array);
        ArrayAdapter<String> adapterEnvironment = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, environments);

        AutoCompleteTextView actEnvironment = view.findViewById(R.id.act_environment);
        actEnvironment.setAdapter(adapterEnvironment);

        String[] location = getResources().getStringArray(R.array.region_array);
        ArrayAdapter<String> adapterLocation = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, location);

        AutoCompleteTextView actLocation = view.findViewById(R.id.act_location);
        actLocation.setAdapter(adapterLocation);

        String[] season = getResources().getStringArray(R.array.seasons_array);
        ArrayAdapter<String> adapterSeason = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, season);

        AutoCompleteTextView actSeason = view.findViewById(R.id.act_season);
        actSeason.setAdapter(adapterSeason);

        String[] year = getResources().getStringArray(R.array.year_array);
        ArrayAdapter<String> adapterYear = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, year);

        AutoCompleteTextView actYear = view.findViewById(R.id.act_year);
        actYear.setAdapter(adapterYear);

        String[] method = getResources().getStringArray(R.array.planting_methods_array);
        ArrayAdapter<String> adapterMethod = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, method);

        AutoCompleteTextView actMethod = view.findViewById(R.id.act_plant_method);
        actMethod.setAdapter(adapterMethod);
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
