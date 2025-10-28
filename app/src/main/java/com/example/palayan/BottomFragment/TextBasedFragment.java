package com.example.palayan.BottomFragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.R;
import com.example.palayan.ResultsAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextBasedFragment extends Fragment {

    private FirebaseFunctions functions;

    private TextInputEditText txtDescription;
    private View btnSearchDisease;
    private ChipGroup chipGroupAffected;
    private TextView tvChipSeasonError;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_based, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        functions = FirebaseFunctions.getInstance("asia-southeast1");

        view.findViewById(R.id.iv_back).setOnClickListener(v -> requireActivity().onBackPressed());

        txtDescription = view.findViewById(R.id.txtDescription);
        btnSearchDisease = view.findViewById(R.id.btnSearchDisease);
        chipGroupAffected = view.findViewById(R.id.chipGroupAffected);
        tvChipSeasonError = view.findViewById(R.id.tvChipSeasonError);

        btnSearchDisease.setOnClickListener(v -> {
            String text = txtDescription.getText() != null ? txtDescription.getText().toString().trim() : "";

            List<Integer> checkedIds = chipGroupAffected.getCheckedChipIds();
            if (checkedIds == null || checkedIds.isEmpty()) {
                tvChipSeasonError.setText("Pumili muna ng kahit isang parte ng halaman.");
                tvChipSeasonError.setVisibility(View.VISIBLE);
                return;
            }

            List<String> affectedParts = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = view.findViewById(id);
                if (chip != null) affectedParts.add(chip.getText().toString().trim());
            }

            tvChipSeasonError.setVisibility(View.GONE);
            runSearch(text, affectedParts);
        });
    }

    private void runSearch(String text, List<String> affectedParts) {
        if (text == null || text.isEmpty()) {
            showToast("Please enter symptoms or description");
            return;
        }
        showToast("Searching...");

        Map<String, Object> payload = new HashMap<>();
        payload.put("text", text);
        payload.put("affectedParts", affectedParts);

        functions.getHttpsCallable("searchDiseaseByText")
                .call(payload)
                .addOnSuccessListener((HttpsCallableResult res) -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) res.getData();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("results");
                    if (results == null || results.isEmpty()) {
                        showToast("Walang tumugmang sakit.");
                        return;
                    }
                    showResultsBottomSheet(results);
                })
                .addOnFailureListener(e -> showToast("Search error: " + e.getMessage()));
    }

    private void showResultsBottomSheet(List<Map<String, Object>> results) {
        if (getActivity() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search_result, null, false);
        RecyclerView rv = v.findViewById(R.id.rvResults);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        ResultsAdapter adapter = new ResultsAdapter(results, item -> {
            Intent i = new Intent(requireContext(), com.example.palayan.UserActivities.ViewResultDisease.class);
            i.putExtra("diseaseName", String.valueOf(item.get("name")));
            startActivity(i);
            dialog.dismiss();
        });
        rv.setAdapter(adapter);
        dialog.setContentView(v);
        dialog.show();
    }

    private void showToast(String msg) {
        if (getActivity() == null) return;
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
    }
}


