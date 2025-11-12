package com.example.palayan.Helper;

import java.io.Serializable;
import java.util.Date;

public class RicePlanting implements Serializable {
    private String id;
    private String riceFieldId;
    private String riceVarietyId; // Document ID from rice_seed_varieties collection
    private String riceVarietyName; // For easy display
    private String plantingDate;
    private Date createdAt;
    private String notes;
    private String plantingMethod;
    private String seedWeight;
    private String fertilizerUsed;
    private String fertilizerAmount;

    public RicePlanting() {
        this.createdAt = new Date();
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public RicePlanting(String riceFieldId, String riceVarietyId, String riceVarietyName, String plantingDate) {
        this.riceFieldId = riceFieldId;
        this.riceVarietyId = riceVarietyId;
        this.riceVarietyName = riceVarietyName;
        this.plantingDate = plantingDate;
        this.createdAt = new Date();
        this.id = String.valueOf(System.currentTimeMillis());
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRiceFieldId() {
        return riceFieldId;
    }

    public void setRiceFieldId(String riceFieldId) {
        this.riceFieldId = riceFieldId;
    }

    public String getRiceVarietyId() {
        return riceVarietyId;
    }

    public void setRiceVarietyId(String riceVarietyId) {
        this.riceVarietyId = riceVarietyId;
    }

    public String getRiceVarietyName() {
        return riceVarietyName;
    }

    public void setRiceVarietyName(String riceVarietyName) {
        this.riceVarietyName = riceVarietyName;
    }

    public String getPlantingDate() {
        return plantingDate;
    }

    public void setPlantingDate(String plantingDate) {
        this.plantingDate = plantingDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPlantingMethod() {
        return plantingMethod;
    }

    public void setPlantingMethod(String plantingMethod) {
        this.plantingMethod = plantingMethod;
    }

    public String getSeedWeight() {
        return seedWeight;
    }

    public void setSeedWeight(String seedWeight) {
        this.seedWeight = seedWeight;
    }

    public String getFertilizerUsed() {
        return fertilizerUsed;
    }

    public void setFertilizerUsed(String fertilizerUsed) {
        this.fertilizerUsed = fertilizerUsed;
    }

    public String getFertilizerAmount() {
        return fertilizerAmount;
    }

    public void setFertilizerAmount(String fertilizerAmount) {
        this.fertilizerAmount = fertilizerAmount;
    }
}

