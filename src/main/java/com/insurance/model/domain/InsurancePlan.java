package com.insurance.model.domain;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class InsurancePlan {
    @JsonProperty("id")
    private String planId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("issuer")
    private Issuer insuranceCompany;

    @JsonProperty("type")
    private String planType;

    @JsonProperty("metal_level")
    private String metalLevel;

    @JsonProperty("premium")
    private String premium;

    @JsonProperty("deductibles")
    private List<Deductible> deductibles;

    @JsonProperty("moops")
    private List<Moop> moops;

    @Data
    public static class Issuer {
        @JsonProperty("name")
        private String name;
    }

    @Data
    public static class Deductible {
        @JsonProperty("type")
        private String type;

        @JsonProperty("amount")
        private Double amount;
    }

    @Data
    public static class Moop {
        @JsonProperty("type")
        private String type;

        @JsonProperty("amount")
        private Double amount;
    }

    public String getInsuranceCompany() {
        return insuranceCompany != null ? insuranceCompany.getName() : "N/A";
    }

    public String getPremium() {
        return premium != null ? "$" + premium : "N/A";
    }

    public Double getPremiumValue() {
        if (premium == null)
            return null;
        return Double.parseDouble(premium);
    }

    public String getDeductible() {
        if (deductibles == null || deductibles.isEmpty())
            return "N/A";
        Deductible deductible = deductibles.stream()
                .filter(d -> "Combined Medical and Drug EHB Deductible".equals(d.getType()))
                .findFirst()
                .orElse(null);
        return deductible != null && deductible.getAmount() != null ? String.format("$%.2f", deductible.getAmount())
                : "N/A";
    }

    public String getMaxOutOfPocket() {
        if (moops == null || moops.isEmpty())
            return "N/A";
        Moop moop = moops.stream()
                .filter(m -> "Maximum Out of Pocket for Medical and Drug EHB Benefits (Total)".equals(m.getType()))
                .findFirst()
                .orElse(null);
        return moop != null && moop.getAmount() != null ? String.format("$%.2f", moop.getAmount()) : "N/A";
    }
}