package com.example.palayan.Helper;

public class TreatmentHistoryModel {
    private String date;
    private String description;

    public TreatmentHistoryModel() {}

    public TreatmentHistoryModel(String date, String description) {
        this.date = date;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
