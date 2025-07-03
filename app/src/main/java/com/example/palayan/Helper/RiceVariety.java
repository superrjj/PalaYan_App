package com.example.palayan.Helper;

public class RiceVariety {
    public String rice_seed_id;
    public String varietyName;
    public String releaseName;
    public String breedingCode;
    public String yearRelease;
    public String breederOrigin;
    public int maturityDays;
    public int plantHeight;
    public double averageYield;
    public double maxYield;
    public int tillers;
    public String location;
    public String environment;
    public String season;
    public String plantingMethod;
    public Boolean archived;

    public RiceVariety() {

    }

    public RiceVariety(String rice_seed_id, String varietyName, String releaseName, String breedingCode, String yearRelease,
                       String breederOrigin, int maturityDays, int plantHeight, double averageYield, double maxYield, int tillers,
                       String location, String environment, String season, String plantingMethod, Boolean archived) {
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
        this.archived = archived;
    }

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

    public int getMaturityDays() {
        return maturityDays;
    }

    public int getPlantHeight() {
        return plantHeight;
    }

    public double getAverageYield() {
        return averageYield;
    }

    public double getMaxYield() {
        return maxYield;
    }

    public int getTillers() {
        return tillers;
    }

    public String getLocation() {
        return location;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getSeason() {
        return season;
    }

    public String getPlantingMethod() {
        return plantingMethod;
    }

    public Boolean getArchived() {
        return archived;
    }
}
