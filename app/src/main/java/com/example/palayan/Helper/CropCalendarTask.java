package com.example.palayan.Helper;

import java.io.Serializable;

public class CropCalendarTask implements Serializable {
    private String id;
    private String taskName; // Task name in Filipino
    private String taskNameEn; // Task name in English
    private int daysAfterPlanting; // Days after planting date
    private String scheduledDate; // Calculated date (yyyy-MM-dd)
    private String weekRange; // Week range display (e.g., "Nov 18–24, 2025")
    private int weekNumber; // Week number (1-16)
    private int taskOrder; // Order of the task within the week
    private boolean isCompleted;
    private String taskType; // "preparation", "fertilizer", "pest_control", "harvest", etc.
    private String taskCategory; // Display label for UI forms
    private String actualCompletionDate; // yyyy-MM-dd selected by user
    private String additionalNotes; // Optional notes/details from the form

    public CropCalendarTask() {
        this.id = String.valueOf(System.currentTimeMillis());
        this.isCompleted = false;
    }

    public CropCalendarTask(String taskName, String taskNameEn, int daysAfterPlanting, String taskType) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.taskName = taskName;
        this.taskNameEn = taskNameEn;
        this.daysAfterPlanting = daysAfterPlanting;
        this.taskType = taskType;
        this.isCompleted = false;
        this.taskCategory = "";
        this.actualCompletionDate = "";
        this.additionalNotes = "";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskNameEn() {
        return taskNameEn;
    }

    public void setTaskNameEn(String taskNameEn) {
        this.taskNameEn = taskNameEn;
    }

    public int getDaysAfterPlanting() {
        return daysAfterPlanting;
    }

    public void setDaysAfterPlanting(int daysAfterPlanting) {
        this.daysAfterPlanting = daysAfterPlanting;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getWeekRange() {
        return weekRange;
    }

    public void setWeekRange(String weekRange) {
        this.weekRange = weekRange;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public int getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(int taskOrder) {
        this.taskOrder = taskOrder;
    }

    public String getTaskCategory() {
        return taskCategory;
    }

    public void setTaskCategory(String taskCategory) {
        this.taskCategory = taskCategory;
    }

    public String getActualCompletionDate() {
        return actualCompletionDate;
    }

    public void setActualCompletionDate(String actualCompletionDate) {
        this.actualCompletionDate = actualCompletionDate;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}

