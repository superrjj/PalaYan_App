package com.example.palayan;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.databinding.ActivityTextBasedBinding;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class TextBased extends AppCompatActivity {

    private ActivityTextBasedBinding root;
    private FirebaseFunctions functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = ActivityTextBasedBinding.inflate(getLayoutInflater());
        setContentView(root.getRoot());

        functions = FirebaseFunctions.getInstance("asia-southeast1");

        root.toolbar.findViewById(R.id.iv_back).setOnClickListener(v -> onBackPressed());

        root.btnSearchDisease.setOnClickListener(v -> {
            String text = root.txtDescription.getText().toString().trim();

            List<Integer> checkedIds = root.chipGroupAffected.getCheckedChipIds();
            if (checkedIds == null || checkedIds.isEmpty()) {
                root.tvChipSeasonError.setText("Pumili muna ng kahit isang parte ng halaman.");
                root.tvChipSeasonError.setVisibility(View.VISIBLE);
                return;
            }

            List<String> affectedParts = new ArrayList<>();
            for (int id : checkedIds) {
                Chip chip = findViewById(id);
                if (chip != null) affectedParts.add(chip.getText().toString().trim());
            }

            root.tvChipSeasonError.setVisibility(View.GONE);
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
        payload.put("affectedParts", affectedParts); // send array for multi-select

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
                .addOnFailureListener(e ->
                        showToast("Search error: " + e.getMessage())
                );
    }

    private void showResultsBottomSheet(List<Map<String, Object>> results) {
        runOnUiThread(() -> {
            BottomSheetDialog dialog = new BottomSheetDialog(TextBased.this);
            View view = getLayoutInflater().inflate(R.layout.dialog_search_result, null, false);
            RecyclerView rv = view.findViewById(R.id.rvResults);
            rv.setLayoutManager(new LinearLayoutManager(TextBased.this));
            ResultsAdapter adapter = new ResultsAdapter(results, item -> {
                showToast("Napili: " + item.get("name"));
                dialog.dismiss();
            });
            rv.setAdapter(adapter);
            dialog.setContentView(view);
            dialog.show();
        });
    }

    private void showToast(String msg) {
        runOnUiThread(() ->
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show()
        );
    }
}