package com.insurance.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlanSearchCriteria {
    private String sortBy;
    private String sortDirection = "asc"; // Default value

    // Filters
    private String insuranceCompany;
    private String planType;
    private String metalLevel;
    private Double premiumMin;
    private Double premiumMax;
    private Double deductibleMin;
    private Double deductibleMax;
    private Double moopMin;
    private Double moopMax;
}