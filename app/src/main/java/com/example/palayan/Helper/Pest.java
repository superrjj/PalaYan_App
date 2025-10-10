package com.example.palayan.Helper;

import com.google.firebase.firestore.PropertyName;

public class Pest {
    public String pest_id;
    public String documentId; // ADDED: To store the Firestore document ID

    @PropertyName("name")
    private String name;

    @PropertyName("scientificName")
    public String scientificName;

    @PropertyName("description")
    public String description;

    @PropertyName("symptoms")
    public String symptoms;

    @PropertyName("cause")
    public String cause;

    @PropertyName("treatments")
    public String treatments;

    @PropertyName("mainImageUrl")
    private String mainImageUrl;

    public boolean archived;
    public boolean isDeleted;

    public Pest() {}

    public Pest(String pest_id, String name, String scientificName, String description, String symptoms, String cause, String treatments,
                String mainImageUrl, boolean archived, boolean isDeleted) {
        this.pest_id = pest_id;
        this.name = name;
        this.scientificName = scientificName;
        this.description = description;
        this.symptoms = symptoms;
        this.cause = cause;
        this.treatments = treatments;
        this.mainImageUrl = mainImageUrl;
        this.archived = archived;
        this.isDeleted = isDeleted;
    }

    // ADDED: Getter and setter for documentId
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("mainImageUrl")
    public String getMainImageUrl() {
        return mainImageUrl;
    }

    @PropertyName("mainImageUrl")
    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public String getPestName() {
        return name;
    }

    public String getPest_id() {
        return pest_id;
    }

    public void setPest_id(String pest_id) {
        this.pest_id = pest_id;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getTreatments() {
        return treatments;
    }

    public void setTreatments(String treatments) {
        this.treatments = treatments;
    }

    public String getImageUrl() {
        return mainImageUrl;
    }
}