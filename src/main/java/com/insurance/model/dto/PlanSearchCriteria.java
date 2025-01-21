package com.insurance.model.dto;

public class PlanSearchCriteria {
    private String metalLevel;
    private String insuranceCompany;
    private String planType;
    private Double premiumMin;
    private Double premiumMax;
    private Double deductibleMin;
    private Double deductibleMax;
    private Double moopMin;
    private Double moopMax;

    // Getters and Setters
    public String getMetalLevel() {
        return metalLevel;
    }

    public void setMetalLevel(String metalLevel) {
        this.metalLevel = metalLevel;
    }

    public String getInsuranceCompany() {
        return insuranceCompany;
    }

    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public Double getPremiumMin() {
        return premiumMin;
    }

    public void setPremiumMin(Double premiumMin) {
        this.premiumMin = premiumMin;
    }

    public Double getPremiumMax() {
        return premiumMax;
    }

    public void setPremiumMax(Double premiumMax) {
        this.premiumMax = premiumMax;
    }

    public Double getDeductibleMin() {
        return deductibleMin;
    }

    public void setDeductibleMin(Double deductibleMin) {
        this.deductibleMin = deductibleMin;
    }

    public Double getDeductibleMax() {
        return deductibleMax;
    }

    public void setDeductibleMax(Double deductibleMax) {
        this.deductibleMax = deductibleMax;
    }

    public Double getMoopMin() {
        return moopMin;
    }

    public void setMoopMin(Double moopMin) {
        this.moopMin = moopMin;
    }

    public Double getMoopMax() {
        return moopMax;
    }

    public void setMoopMax(Double moopMax) {
        this.moopMax = moopMax;
    }
}
