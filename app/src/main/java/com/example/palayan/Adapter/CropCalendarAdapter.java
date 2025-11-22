package com.example.palayan.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.palayan.Helper.CropCalendarTask;
import com.example.palayan.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CropCalendarAdapter extends RecyclerView.Adapter<CropCalendarAdapter.WeekViewHolder> {

    private List<List<CropCalendarTask>> weeksList; // Tasks grouped by week
    private OnTaskCheckListener listener;
    private int maxUnlockedWeek = Integer.MAX_VALUE;

    public interface OnTaskCheckListener {
        void onTaskCheckRequested(CropCalendarTask task, boolean targetState);
    }

    public CropCalendarAdapter(OnTaskCheckListener listener) {
        this.listener = listener;
        this.weeksList = new ArrayList<>();
    }

    @NonNull
    @Override
    public WeekViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop_calendar_task, parent, false);
        return new WeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekViewHolder holder, int position) {
        android.util.Log.d("CropCalendarAdapter", "onBindViewHolder called for position: " + position + " (total weeks: " + weeksList.size() + ")");
        List<CropCalendarTask> weekTasks = weeksList.get(position);
        if (weekTasks == null || weekTasks.isEmpty()) {
            android.util.Log.d("CropCalendarAdapter", "Week tasks is null or empty for position: " + position);
            return;
        }

        // Set week range without week number (use first task's week range)
        CropCalendarTask firstTask = weekTasks.get(0);
        holder.tvWeekRange.setText(firstTask.getWeekRange());

        // Clear existing task items
        holder.layoutTasks.removeAllViews();

        // Add tasks for this week
        for (CropCalendarTask task : weekTasks) {
            View taskView = LayoutInflater.from(holder.itemView.getContext())
                    .inflate(R.layout.item_crop_calendar_task_item, holder.layoutTasks, false);
            
            CheckBox cbTask = taskView.findViewById(R.id.cbTaskCompleted);
            TextView tvTaskName = taskView.findViewById(R.id.tvTaskName);

            cbTask.setChecked(task.isCompleted());
            tvTaskName.setText("➤ " + task.getTaskName());

            boolean isEnabled = task.getWeekNumber() <= maxUnlockedWeek || task.isCompleted();
            cbTask.setEnabled(isEnabled);
            cbTask.setAlpha(isEnabled ? 1f : 0.5f);

            cbTask.setOnClickListener(v -> {
                if (!isEnabled) {
                    cbTask.setChecked(false);
                    Toast.makeText(holder.itemView.getContext(), "Tapusin muna ang mga gawain sa kasalukuyang linggo.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null) {
                    if (task.isCompleted()) {
                        cbTask.setChecked(true); // Keep it checked
                        listener.onTaskCheckRequested(task, true);
                    } else {
                        cbTask.setChecked(task.isCompleted()); // Revert temporarily
                        listener.onTaskCheckRequested(task, true);
                    }
                } else {
                    cbTask.setChecked(task.isCompleted());
                }
            });

            holder.layoutTasks.addView(taskView);
        }
    }

    @Override
    public int getItemCount() {
        int count = weeksList != null ? weeksList.size() : 0;
        android.util.Log.d("CropCalendarAdapter", "getItemCount() called, returning: " + count);
        return count;
    }

    public void updateTasks(List<CropCalendarTask> tasks) {
        weeksList.clear();
        
        if (tasks == null || tasks.isEmpty()) {
            android.util.Log.d("CropCalendarAdapter", "No tasks to display");
            notifyDataSetChanged();
            return;
        }

        android.util.Log.d("CropCalendarAdapter", "updateTasks called with " + tasks.size() + " tasks");

        // Group tasks by week number
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

        android.util.Log.d("CropCalendarAdapter", "Grouped into " + weeksList.size() + " weeks. getItemCount() will return: " + weeksList.size());
        notifyDataSetChanged();
    }

    public void setMaxUnlockedWeek(int maxUnlockedWeek) {
        if (maxUnlockedWeek <= 0) {
            this.maxUnlockedWeek = Integer.MAX_VALUE;
        } else {
            this.maxUnlockedWeek = maxUnlockedWeek;
        }
        notifyDataSetChanged();
    }

    private String formatDateShort(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    static class WeekViewHolder extends RecyclerView.ViewHolder {
        TextView tvWeekRange;
        LinearLayout layoutTasks;

        public WeekViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWeekRange = itemView.findViewById(R.id.tvWeekRange);
            layoutTasks = itemView.findViewById(R.id.layoutTasks);
        }
    }
}

