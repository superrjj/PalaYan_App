package com.example.palayan.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RiceFieldProfile implements Serializable {
    private String id;
    private String name;
    private String imageUrl;
    private String province;
    private String city;
    private String barangay;
    private double sizeHectares;
    private String soilType;
    private Date createdAt;
    private List<HistoryEntry> history;

    public RiceFieldProfile() {
        this.history = new ArrayList<>();
        this.createdAt = new Date();
    }

    public RiceFieldProfile(String name, String imageUrl, String province, String city, String barangay, double sizeHectares, String soilType) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.province = province;
        this.city = city;
        this.barangay = barangay;
        this.sizeHectares = sizeHectares;
        this.soilType = soilType;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getSoilType() {
        return soilType;
    }

    public void setSoilType(String soilType) {
        this.soilType = soilType;
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

