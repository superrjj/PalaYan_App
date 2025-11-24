package com.example.palayan.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.CropCalendarTask;
import com.example.palayan.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskHistoryAdapter extends RecyclerView.Adapter<TaskHistoryAdapter.ViewHolder> {

    private final Context context;
    private final List<CropCalendarTask> completedTasks;

    public TaskHistoryAdapter(Context context, List<CropCalendarTask> completedTasks) {
        this.context = context;
        this.completedTasks = completedTasks != null ? completedTasks : new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CropCalendarTask task = completedTasks.get(position);

        // Set task name
        holder.tvTaskName.setText(task.getTaskName());

        // Set completion date
        String completionDate = task.getActualCompletionDate();
        if (completionDate != null && !completionDate.isEmpty()) {
            try {
                // Parse yyyy-MM-dd format
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = inputFormat.parse(completionDate);
                if (date != null) {
                    // Format to readable date (e.g., "Nov 23, 2025")
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    holder.tvDate.setText(outputFormat.format(date));
                } else {
                    holder.tvDate.setText(completionDate);
                }
            } catch (ParseException e) {
                holder.tvDate.setText(completionDate);
            }
        } else {
            holder.tvDate.setText("Hindi nakalista");
        }

        // Set description/notes
        String notes = task.getAdditionalNotes();
        if (notes != null && !notes.trim().isEmpty()) {
            holder.tvDescription.setText(notes);
            holder.tvDescription.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescription.setText("Walang deskripsyon");
            holder.tvDescription.setVisibility(View.VISIBLE);
        }

        // Set week range if available
        String weekRange = task.getWeekRange();
        if (weekRange != null && !weekRange.isEmpty()) {
            holder.tvWeekRange.setText("Linggo: " + weekRange);
            holder.tvWeekRange.setVisibility(View.VISIBLE);
        } else {
            holder.tvWeekRange.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return completedTasks.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvDate, tvDescription, tvWeekRange;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvWeekRange = itemView.findViewById(R.id.tvWeekRange);
        }
    }
}

