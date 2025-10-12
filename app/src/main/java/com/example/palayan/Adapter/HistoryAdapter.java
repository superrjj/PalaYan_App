package com.example.palayan.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.palayan.Helper.HistoryResult;
import com.example.palayan.R;
import com.example.palayan.UserActivities.SaveOnlyDetails;
import com.example.palayan.UserActivities.TreatmentAppliedDetails;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

        // Set timestamp as date with proper timezone handling
        if (history.getTimestamp() != null) {
            try {
                Date date = history.getTimestamp();

                // Debug logging to see what's happening
                Log.d("HistoryAdapter", "Raw timestamp: " + date.toString());
                Log.d("HistoryAdapter", "Raw timestamp millis: " + date.getTime());

                // Use UTC+8 timezone to match Firestore
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC+8")); // Match Firestore timezone

                String formattedDate = sdf.format(date);

                Log.d("HistoryAdapter", "Formatted date (UTC+8): " + formattedDate);

                // Try different timezone formats for comparison
                SimpleDateFormat sdfDefault = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String formattedDateDefault = sdfDefault.format(date);
                Log.d("HistoryAdapter", "Formatted date (default): " + formattedDateDefault);

                // Use the UTC+8 version
                historyHolder.textDate.setText(formattedDate);
            } catch (Exception e) {
                Log.e("HistoryAdapter", "Date formatting error: " + e.getMessage());
                historyHolder.textDate.setText("Invalid Date");
            }
        } else {
            historyHolder.textDate.setText("No Date");
        }

        // Load image (works for both imageUrl and photoUrl)
        String imageUrl = history.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.loading_image)
                    .into(historyHolder.imgDisease);
        }

        // Set "View details" text based on type
        String type = history.getType();
        if ("treatment".equals(type)) {
            historyHolder.textViewDetails.setText("Treatment applied");
        } else {
            historyHolder.textViewDetails.setText("View details");
        }

        // Add click listener to navigate to appropriate details activity
        historyHolder.cardView.setOnClickListener(v -> {
            String historyType = history.getType();

            if ("prediction".equals(historyType)) {
                // Navigate to SaveOnlyDetails for predictions_result
                Intent intent = new Intent(context, SaveOnlyDetails.class);
                intent.putExtra("history_id", history.getDocumentId());
                intent.putExtra("device_id", history.getUserId());
                intent.putExtra("history_type", "prediction");
                context.startActivity(intent);
            } else if ("treatment".equals(historyType)) {
                // Navigate to TreatmentAppliedDetails for treatment_notes
                Intent intent = new Intent(context, TreatmentAppliedDetails.class);
                intent.putExtra("history_id", history.getDocumentId());
                intent.putExtra("device_id", history.getUserId());
                intent.putExtra("history_type", "treatment");
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public class HistoryHolder extends RecyclerView.ViewHolder {
        TextView textTitle, textDate, textViewDetails;
        ShapeableImageView imgDisease;
        MaterialCardView cardView;

        public HistoryHolder(@NonNull View itemView) {
            super(itemView);

            // Match the actual IDs from your layout
            textTitle = itemView.findViewById(R.id.text_title);
            textDate = itemView.findViewById(R.id.text_date);
            textViewDetails = itemView.findViewById(R.id.text_view_details);
            imgDisease = itemView.findViewById(R.id.img_disease);
            cardView = (MaterialCardView) itemView; // The root view is the MaterialCardView
        }
    }
}