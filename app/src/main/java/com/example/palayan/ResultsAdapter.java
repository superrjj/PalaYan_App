package com.example.palayan;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.VH> {

    public interface OnItemClick {
        void onClick(Map<String, Object> item);
    }

    private final List<Map<String, Object>> items;
    private final OnItemClick onItemClick;

    public ResultsAdapter(List<Map<String, Object>> items, OnItemClick onItemClick) {
        this.items = items;
        this.onItemClick = onItemClick;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disease_result, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Map<String, Object> m = items.get(position);
        String name = String.valueOf(m.get("name"));
        String sci = String.valueOf(m.get("scientificName"));
        double score = 0.0;
        try { score = Double.parseDouble(String.valueOf(m.get("score"))); } catch (Exception ignored) {}
        @SuppressWarnings("unchecked")
        List<String> tokens = (List<String>) m.get("matchedTokens");

        h.tvName.setText(name);
        h.tvScientific.setText(sci.equals("null") ? "" : sci);
        h.tvScore.setText(String.format(Locale.US, "score: %.3f", score));
        h.tvTokens.setText(tokens == null ? "" : "tokens: " + tokens);

        h.itemView.setOnClickListener(v -> {
            if (onItemClick != null) onItemClick.onClick(m);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvScientific, tvScore, tvTokens;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvScientific = itemView.findViewById(R.id.tvScientific);
            tvScore = itemView.findViewById(R.id.tvScore);
            tvTokens = itemView.findViewById(R.id.tvTokens);
        }
    }
}