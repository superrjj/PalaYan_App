package com.example.palayan.Helper;

import java.util.Date;

public class TreatmentTimelineModel {
    private String photoUrl;
    private String date;
    private String description;
    private Date timestamp;

    public TreatmentTimelineModel() {}

    public TreatmentTimelineModel(String photoUrl, String date, String description, Date timestamp) {
        this.photoUrl = photoUrl;
        this.date = date;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}

