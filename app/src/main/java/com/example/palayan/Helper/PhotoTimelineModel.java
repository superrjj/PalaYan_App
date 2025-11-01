package com.example.palayan.Helper;

public class PhotoTimelineModel {
    private String imageUrl;
    private String date;
    private String description;

    public PhotoTimelineModel() {}

    public PhotoTimelineModel(String imageUrl, String date) {
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public PhotoTimelineModel(String imageUrl, String date, String description) {
        this.imageUrl = imageUrl;
        this.date = date;
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}