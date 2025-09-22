package com.example.palayan.Helper;

import java.util.List;

public class RiceVariety {
    public String rice_seed_id;
    public String varietyName;
    public String releaseName;
    public String breedingCode;
    public String yearRelease;
    public String breederOrigin;
    public String maturityDays;        // Changed from int to String
    public String plantHeight;         // Changed from int to String
    public String averageYield;        // Changed from double to String
    public String maxYield;            // Changed from double to String
    public String tillers;             // Changed from int to String
    public String location;
    public List<String> environment;   // Changed from String to List<String>
    public List<String> season;        // Changed from String to List<String>
    public String plantingMethod;
    public Boolean isDeleted;          // Changed from archived to isDeleted
    public Boolean recommendedInTarlac; // Added new field

    public RiceVariety() {

    }

    public RiceVariety(String rice_seed_id, String varietyName, String releaseName, String breedingCode, String yearRelease,
                       String breederOrigin, String maturityDays, String plantHeight, String averageYield, String maxYield, String tillers,
                       String location, List<String> environment, List<String> season, String plantingMethod, Boolean isDeleted, Boolean recommendedInTarlac) {
        this.rice_seed_id = rice_seed_id;
        this.varietyName = varietyName;
        this.releaseName = releaseName;
        this.breedingCode = breedingCode;
        this.yearRelease = yearRelease;
        this.breederOrigin = breederOrigin;
        this.maturityDays = maturityDays;
        this.tillers = tillers;
        this.plantHeight = plantHeight;
        this.averageYield = averageYield;
        this.maxYield = maxYield;
        this.location = location;
        this.environment = environment;
        this.season = season;
        this.plantingMethod = plantingMethod;
        this.isDeleted = isDeleted;
        this.recommendedInTarlac = recommendedInTarlac;
    }

    // Getters
    public String getRice_seed_id() {
        return rice_seed_id;
    }

    public String getVarietyName() {
        return varietyName;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public String getBreedingCode() {
        return breedingCode;
    }

    public String getYearRelease() {
        return yearRelease;
    }

    public String getBreederOrigin() {
        return breederOrigin;
    }

    public String getMaturityDays() {
        return maturityDays;
    }

    public String getPlantHeight() {
        return plantHeight;
    }

    public String getAverageYield() {
        return averageYield;
    }

    public String getMaxYield() {
        return maxYield;
    }

    public String getTillers() {
        return tillers;
    }

    public String getLocation() {
        return location;
    }

    public List<String> getEnvironment() {
        return environment;
    }

    public List<String> getSeason() {
        return season;
    }

    public String getPlantingMethod() {
        return plantingMethod;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public Boolean getRecommendedInTarlac() {
        return recommendedInTarlac;
    }
}