package com.example.palayan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.CropCalendarTask;
import com.example.palayan.R;

import java.util.ArrayList;
import java.util.List;

public class CropCalendarPreviewAdapter extends RecyclerView.Adapter<CropCalendarPreviewAdapter.WeekViewHolder> {

    private List<CropCalendarTask> tasks;

    public CropCalendarPreviewAdapter() {
        this.tasks = new ArrayList<>();
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_calendar_preview, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        CropCalendarTask task = tasks.get(position);
        if (task == null) {
            return;
        }

        // Format: "Oct 1 - Oct 7" (remove year, use dash)
        String weekRange = task.getWeekRange();
        // Convert "Nov 18–24, 2025" to "Nov 18 - Nov 24" format
        if (weekRange.contains("–")) {
            // Split by en-dash
            String[] parts = weekRange.split("–");
            if (parts.length == 2) {
                String startPart = parts[0].trim();
                String endPart = parts[1].trim();
                // Remove year from end part
                if (endPart.contains(",")) {
                    endPart = endPart.substring(0, endPart.lastIndexOf(",")).trim();
                }
                // Extract month and day from start
                String[] startWords = startPart.split(" ");
                if (startWords.length >= 2) {
                    String month = startWords[0];
                    String day = startWords[1];
                    // Extract day from end part
                    String[] endWords = endPart.split(" ");
                    if (endWords.length >= 1) {
                        String endDay = endWords[endWords.length - 1];
                        weekRange = month + " " + day + " - " + month + " " + endDay;
                    }
                }
            }
        } else if (weekRange.contains("-")) {
            // Already in correct format, just remove year
            if (weekRange.contains(",")) {
                weekRange = weekRange.substring(0, weekRange.lastIndexOf(",")).trim();
            }
        }
        holder.tvWeekRange.setText(weekRange);
        holder.tvTaskName.setText(task.getTaskName());
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public void updateTasks(List<CropCalendarTask> tasks) {
        this.tasks = tasks != null ? new ArrayList<>(tasks) : new ArrayList<>();
        android.util.Log.d("CropCalendarPreview", "Adapter updated with " + this.tasks.size() + " tasks");
        notifyDataSetChanged();
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekRange;
        TextView tvTaskName;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekRange = itemView.findViewById(R.id.tvWeekRange);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
        }
    }
}

