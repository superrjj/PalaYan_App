package com.example.palayan.Helper;

public class Pest {
    public String pest_id;
    public String pestName;
    public String scientificName;
    public String description;
    public String cause;
    public String treatments;
    public String imageUrl;

    public Pest() {}

    public Pest(String pest_id, String pestName, String scientificName, String description, String cause, String treatments, String imageUrl) {
        this.pest_id = pest_id;
        this.pestName = pestName;
        this.scientificName = scientificName;
        this.description = description;
        this.cause = cause;
        this.treatments = treatments;
        this.imageUrl = imageUrl;
    }

    public String getPestName() {
        return pestName;
    }

    public String getPest_id() {
        return pest_id;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescription() {
        return description;
    }

    public String getCause() {
        return cause;
    }

    public String getTreatments() {
        return treatments;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
