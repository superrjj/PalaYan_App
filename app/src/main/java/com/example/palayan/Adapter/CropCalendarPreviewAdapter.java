package com.example.palayan.Adapter;

import android.text.TextUtils;
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

    private List<List<CropCalendarTask>> weeksList;

    public CropCalendarPreviewAdapter() {
        this.weeksList = new ArrayList<>();
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_calendar_preview, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        if (weeksList == null || position < 0 || position >= weeksList.size()) {
            return;
        }

        List<CropCalendarTask> weekTasks = weeksList.get(position);
        if (weekTasks == null || weekTasks.isEmpty()) {
            return;
        }

        CropCalendarTask firstTask = weekTasks.get(0);
        holder.tvWeekRange.setText(formatWeekRange(firstTask.getWeekRange()));

        List<String> taskNames = new ArrayList<>();
        for (CropCalendarTask task : weekTasks) {
            taskNames.add(task.getTaskName());
        }
        holder.tvTasks.setText(TextUtils.join("\n", taskNames));
    }

    @Override
    public int getItemCount() {
        return weeksList != null ? weeksList.size() : 0;
    }

    public void updateTasks(List<CropCalendarTask> tasks) {
        weeksList.clear();

        if (tasks == null || tasks.isEmpty()) {
            android.util.Log.d("CropCalendarPreview", "No tasks to display");
            notifyDataSetChanged();
            return;
        }

        int currentWeekNum = -1;
        List<CropCalendarTask> currentWeekTasks = new ArrayList<>();

        for (CropCalendarTask task : tasks) {
            int taskWeekNum = task.getWeekNumber();
            if (taskWeekNum != currentWeekNum) {
                if (!currentWeekTasks.isEmpty()) {
                    weeksList.add(new ArrayList<>(currentWeekTasks));
                }
                currentWeekTasks.clear();
                currentWeekNum = taskWeekNum;
            }
            currentWeekTasks.add(task);
        }

        if (!currentWeekTasks.isEmpty()) {
            weeksList.add(currentWeekTasks);
        }

        android.util.Log.d("CropCalendarPreview", "Adapter grouped into " + weeksList.size() + " weeks");
        notifyDataSetChanged();
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekRange;
        TextView tvTasks;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekRange = itemView.findViewById(R.id.tvWeekRange);
            tvTasks = itemView.findViewById(R.id.tvTasks);
        }
    }

    private String formatWeekRange(String originalRange) {
        if (originalRange == null || originalRange.isEmpty()) {
            return "";
        }

        String weekRange = originalRange;
        if (weekRange.contains("–")) {
            String[] parts = weekRange.split("–");
            if (parts.length == 2) {
                String startPart = parts[0].trim();
                String endPart = parts[1].trim();
                String year = "";
                if (endPart.contains(",")) {
                    year = endPart.substring(endPart.lastIndexOf(",") + 1).trim();
                    endPart = endPart.substring(0, endPart.lastIndexOf(",")).trim();
                }
                if (startPart.contains(",")) {
                    startPart = startPart.substring(0, startPart.lastIndexOf(",")).trim();
                }
                weekRange = startPart + " - " + endPart;
                if (!year.isEmpty()) {
                    weekRange = weekRange + ", " + year;
                }
                return weekRange;
            }
        }
        return weekRange;
    }
}

