package com.example.palayan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.HistoryResult;
import com.example.palayan.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {

    private List<HistoryResult> historyList;
    private Context context;

    public HistoryAdapter(List<HistoryResult> historyList, Context context) {
        this.historyList = historyList;
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryAdapter.HistoryHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_history_result, viewGroup, false);
        return new HistoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryHolder historyHolder, int position) {
        HistoryResult history = historyList.get(position);

        // Set disease name as title
        historyHolder.textTitle.setText(history.getDiseaseName());

        // Set timestamp as date
        if (history.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            historyHolder.textDate.setText(sdf.format(history.getTimestamp()));
        }

        // Load image (works for both imageUrl and photoUrl)
        String imageUrl = history.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.loading_image)
                    .into(historyHolder.imgDisease);
        }

        // Set "View details" text (you can customize this based on type)
        String type = history.getType();
        if ("treatment".equals(type)) {
            historyHolder.textViewDetails.setText("Treatment applied");
        } else {
            historyHolder.textViewDetails.setText("View details");
        }
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class HistoryHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDate, textViewDetails;
        ImageView imgDisease;
        CardView cardView;

        public HistoryHolder(@NonNull View itemView) {
            super(itemView);

            // Match the actual IDs from your layout
            textTitle = itemView.findViewById(R.id.text_title);
            textDate = itemView.findViewById(R.id.text_date);
            textViewDetails = itemView.findViewById(R.id.text_view_details);
            imgDisease = itemView.findViewById(R.id.img_disease);
            cardView = (CardView) itemView; // The root view is the CardView
        }
    }
}