package com.example.palayan.Helper;

import com.google.firebase.firestore.PropertyName;
import java.util.Date;

public class HistoryResult {
    public String documentId; // Firestore document ID
    public String userId; // User ID who made the scan

    @PropertyName("imageUrl")
    private String imageUrl; // For predictions_result

    @PropertyName("photoUrl")
    private String photoUrl; // For treatment_notes

    @PropertyName("diseaseName")
    public String diseaseName; // The predicted disease/pest name

    @PropertyName("scientificName")
    public String scientificName; // Scientific name of the disease/pest

    @PropertyName("description")
    public String description;

    @PropertyName("symptoms")
    public String symptoms;

    @PropertyName("causes")
    public String causes;

    @PropertyName("treatments")
    public String treatments;

    @PropertyName("timestamp")
    public Date timestamp;

    @PropertyName("deviceId")
    public String deviceId;

    // For treatment_notes only
    @PropertyName("dateApplied")
    public String dateApplied;

    public HistoryResult() {}

    // Helper method to get the image URL (works for both collections)
    public String getImageUrl() {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        return photoUrl;
    }

    // Helper method to determine if this is a prediction or treatment
    public String getType() {
        return dateApplied != null ? "treatment" : "prediction";
    }

    // Getters and Setters
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @PropertyName("photoUrl")
    public String getPhotoUrl() {
        return photoUrl;
    }

    @PropertyName("photoUrl")
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @PropertyName("diseaseName")
    public String getDiseaseName() {
        return diseaseName;
    }

    @PropertyName("diseaseName")
    public void setDiseaseName(String diseaseName) {
        this.diseaseName = diseaseName;
    }

    @PropertyName("scientificName")
    public String getScientificName() {
        return scientificName;
    }

    @PropertyName("scientificName")
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("symptoms")
    public String getSymptoms() {
        return symptoms;
    }

    @PropertyName("symptoms")
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    @PropertyName("causes")
    public String getCauses() {
        return causes;
    }

    @PropertyName("causes")
    public void setCauses(String causes) {
        this.causes = causes;
    }

    @PropertyName("treatments")
    public String getTreatments() {
        return treatments;
    }

    @PropertyName("treatments")
    public void setTreatments(String treatments) {
        this.treatments = treatments;
    }

    @PropertyName("timestamp")
    public Date getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("deviceId")
    public String getDeviceId() {
        return deviceId;
    }

    @PropertyName("deviceId")
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @PropertyName("dateApplied")
    public String getDateApplied() {
        return dateApplied;
    }

    @PropertyName("dateApplied")
    public void setDateApplied(String dateApplied) {
        this.dateApplied = dateApplied;
    }
}