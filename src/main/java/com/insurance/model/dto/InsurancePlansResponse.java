package com.insurance.model.dto;

import com.insurance.model.domain.InsurancePlan;
import lombok.Data;
import java.util.List;

@Data
public class InsurancePlansResponse {
    private List<InsurancePlan> plans;
    private int total;
}