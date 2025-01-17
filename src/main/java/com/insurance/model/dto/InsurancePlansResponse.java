package com.insurance.model.dto;

import java.util.List;

import com.insurance.model.domain.InsurancePlan;

import lombok.Data;

/**
 * Response object for the Healthcare.gov API plan search
 * Contains the list of plans and total count information
 */
@Data
public class InsurancePlansResponse {
    private List<InsurancePlan> plans;
    private int total;
    private int offset;
    private int limit;
}