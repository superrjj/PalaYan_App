package com.example.palayan.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RiceFieldProfile implements Serializable {
    private String id;
    private String name;
    private double sizeHectares;
    private String soilType;
    private String riceVariety;
    private String plantingDate;
    private Date createdAt;
    private List<HistoryEntry> history;

    public RiceFieldProfile() {
        this.history = new ArrayList<>();
        this.createdAt = new Date();
    }

    public RiceFieldProfile(String name, double sizeHectares, String soilType, String riceVariety, String plantingDate) {
        this.name = name;
        this.sizeHectares = sizeHectares;
        this.soilType = soilType;
        this.riceVariety = riceVariety;
        this.plantingDate = plantingDate;
        this.history = new ArrayList<>();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSizeHectares() {
        return sizeHectares;
    }

    public void setSizeHectares(double sizeHectares) {
        this.sizeHectares = sizeHectares;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
    }

    public String getRiceVariety() {
        return riceVariety;
    }

    public void setRiceVariety(String riceVariety) {
        this.riceVariety = riceVariety;
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

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public void setHistory(List<HistoryEntry> history) {
        this.history = history;
    }

    // History Entry Inner Class
    public static class HistoryEntry implements Serializable {
        private String id;
        private String type; // "fertilizer", "pesticide", "photo", "harvest"
        private String date;
        private double amount; // For fertilizer, pesticide, harvest
        private String unit; // "kg", "L", "sacks"
        private String photoPath; // For photo entries
        private String description; // For all entries
        private Date timestamp;

        public HistoryEntry() {
            this.timestamp = new Date();
            this.id = String.valueOf(System.currentTimeMillis());
        }

        public HistoryEntry(String type, String date, double amount, String unit, String description) {
            this.type = type;
            this.date = date;
            this.amount = amount;
            this.unit = unit;
            this.description = description;
            this.timestamp = new Date();
            this.id = String.valueOf(System.currentTimeMillis());
        }

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getPhotoPath() {
            return photoPath;
        }

        public void setPhotoPath(String photoPath) {
            this.photoPath = photoPath;
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
}

