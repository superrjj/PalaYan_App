package com.example.palayan.Helper;

public class PhotoTimelineModel {
    private String imageUrl;
    private String date;

    public PhotoTimelineModel() {}

    public PhotoTimelineModel(String imageUrl, String date) {
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDate() {
        return date;
    }
}