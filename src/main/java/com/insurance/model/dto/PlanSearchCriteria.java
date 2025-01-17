package com.insurance.model.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for insurance plan search criteria
 * Contains all possible filter parameters for plan searches
 */
@Getter
@Setter
@NoArgsConstructor
public class PlanSearchCriteria {
    private String sortBy;
    private String sortDirection = "asc"; // Default value

    // Search filters
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