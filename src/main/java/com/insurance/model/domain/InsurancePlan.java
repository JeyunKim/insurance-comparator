package com.insurance.model.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

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
    private Double premium;

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
        return premium != null ? String.format("$%.2f", premium) : "N/A";
    }

    public Double getPremiumValue() {
        return premium;
    }

    public String getDeductible() {
        return formatAmount(getDeductibleValue());
    }

    public Double getDeductibleValue() {
        if (deductibles == null || deductibles.isEmpty()) {
            return 0.0;
        }
        return deductibles.stream()
                .filter(d -> "Combined Medical and Drug EHB Deductible".equals(d.getType()))
                .map(Deductible::getAmount)
                .findFirst()
                .orElse(0.0);
    }

    public String getMaxOutOfPocket() {
        return formatAmount(getMaxOutOfPocketValue());
    }

    public Double getMaxOutOfPocketValue() {
        if (moops == null || moops.isEmpty()) {
            return null;
        }
        return moops.stream()
                .filter(m -> "Maximum Out of Pocket for Medical and Drug EHB Benefits (Total)".equals(m.getType()))
                .map(Moop::getAmount)
                .findFirst()
                .orElse(null);
    }

    private String formatAmount(Double amount) {
        if (amount == null) {
            return "N/A";
        }
        return String.format("$%.2f", amount);
    }
}
