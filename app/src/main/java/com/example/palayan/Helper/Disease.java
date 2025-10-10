package com.example.palayan.Helper;

import com.google.firebase.firestore.PropertyName;
import java.util.List;

public class Disease {
    public String disease_id;
    public String documentId; // To store the Firestore document ID

    @PropertyName("name")
    private String name;

    @PropertyName("scientificName")
    public String scientificName;

    @PropertyName("localName")
    public String localName;

    @PropertyName("description")
    public String description;

    @PropertyName("symptoms")
    public String symptoms;

    @PropertyName("cause")
    public String cause;

    @PropertyName("treatments")
    public String treatments;

    @PropertyName("affectedParts")
    public List<String> affectedParts; // Array of strings from Firebase

    @PropertyName("images")
    private List<String> images; // Array of image URLs from Firebase

    public boolean archived;
    public boolean isDeleted;

    public Disease() {}

    public Disease(String disease_id, String name, String scientificName, String localName, String description,
                   String symptoms, String cause, String treatments, List<String> affectedParts,
                   List<String> images, boolean archived, boolean isDeleted) {
        this.disease_id = disease_id;
        this.name = name;
        this.scientificName = scientificName;
        this.localName = localName;
        this.description = description;
        this.symptoms = symptoms;
        this.cause = cause;
        this.treatments = treatments;
        this.affectedParts = affectedParts;
        this.images = images;
        this.archived = archived;
        this.isDeleted = isDeleted;
    }

    // Getter and setter for documentId
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

    @PropertyName("scientificName")
    public String getScientificName() {
        return scientificName;
    }

    @PropertyName("scientificName")
    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    @PropertyName("localName")
    public String getLocalName() {
        return localName;
    }

    @PropertyName("localName")
    public void setLocalName(String localName) {
        this.localName = localName;
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

    @PropertyName("cause")
    public String getCause() {
        return cause;
    }

    @PropertyName("cause")
    public void setCause(String cause) {
        this.cause = cause;
    }

    @PropertyName("treatments")
    public String getTreatments() {
        return treatments;
    }

    @PropertyName("treatments")
    public void setTreatments(String treatments) {
        this.treatments = treatments;
    }

    @PropertyName("affectedParts")
    public List<String> getAffectedParts() {
        return affectedParts;
    }

    @PropertyName("affectedParts")
    public void setAffectedParts(List<String> affectedParts) {
        this.affectedParts = affectedParts;
    }

    @PropertyName("images")
    public List<String> getImages() {
        return images;
    }

    @PropertyName("images")
    public void setImages(List<String> images) {
        this.images = images;
    }

    // Get main image URL from the first image in the array
    public String getMainImageUrl() {
        return (images != null && !images.isEmpty()) ? images.get(0) : null;
    }

    // REMOVED: setMainImageUrl method since we don't have a mainImageUrl field
    // The mainImageUrl is derived from the images list

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

    public String getDisease_id() {
        return disease_id;
    }

    public void setDisease_id(String disease_id) {
        this.disease_id = disease_id;
    }
}