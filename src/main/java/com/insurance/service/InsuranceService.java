package com.insurance.service;

import com.insurance.client.HealthcareGovClient;
import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.domain.StateMarketplace;
import com.insurance.model.dto.PlanSearchCriteria;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceService {
    private final HealthcareGovClient healthcareGovClient;

    public List<InsurancePlan> getInsurancePlans(String zipCode, int year, PlanSearchCriteria criteria) {
        InsurancePlan[] plans = healthcareGovClient.getInsurancePlans(zipCode, year);

        log.info("Total plans fetched: {}", plans.length);

        // Log deductible values for all plans
        for (InsurancePlan plan : plans) {
            log.info("Plan deductible check - name: {}, company: {}, deductible: {}, type: {}",
                    plan.getName(),
                    plan.getInsuranceCompany(),
                    plan.getDeductible(),
                    plan.getDeductible() != null ? plan.getDeductible().getClass().getName() : "null");
        }

        List<InsurancePlan> filteredPlans = Arrays.asList(plans);

        if (criteria == null) {
            return filteredPlans;
        }

        log.info("Total plans before filtering: {}", filteredPlans.size());

        // Filter by Metal Level
        if (StringUtils.hasText(criteria.getMetalLevel())) {
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> plan.getMetalLevel().equalsIgnoreCase(criteria.getMetalLevel()))
                    .collect(Collectors.toList());
        }

        // Filter by Insurance Company
        if (StringUtils.hasText(criteria.getInsuranceCompany())) {
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> plan.getInsuranceCompany().toLowerCase()
                            .contains(criteria.getInsuranceCompany().toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Filter by Plan Type
        if (StringUtils.hasText(criteria.getPlanType())) {
            filteredPlans = filteredPlans.stream()
                    .filter(plan -> plan.getPlanType().equals(criteria.getPlanType()))
                    .collect(Collectors.toList());
        }

        // Filter by Premium Range
        if (criteria.getPremiumMin() != null) {
            filteredPlans = filteredPlans.stream()
                    .filter(p -> p.getPremiumValue() != null && p.getPremiumValue() >= criteria.getPremiumMin())
                    .collect(Collectors.toList());
        }
        if (criteria.getPremiumMax() != null) {
            filteredPlans = filteredPlans.stream()
                    .filter(p -> p.getPremiumValue() != null && p.getPremiumValue() <= criteria.getPremiumMax())
                    .collect(Collectors.toList());
        }

        // Filter by Deductible Range
        if (criteria.getDeductibleMin() != null) {
            filteredPlans = filteredPlans.stream()
                    .filter(p -> p.getDeductibleValue() != null
                            && p.getDeductibleValue() >= criteria.getDeductibleMin())
                    .collect(Collectors.toList());
        }
        if (criteria.getDeductibleMax() != null) {
            filteredPlans = filteredPlans.stream()
                    .filter(p -> p.getDeductibleValue() != null
                            && p.getDeductibleValue() <= criteria.getDeductibleMax())
                    .collect(Collectors.toList());
        }

        // Filter by Max Out of Pocket Range
        if (criteria.getMoopMin() != null) {
            filteredPlans = filteredPlans.stream()
                    .filter(p -> p.getMaxOutOfPocketValue() != null
                            && p.getMaxOutOfPocketValue() >= criteria.getMoopMin())
                    .collect(Collectors.toList());
        }
        if (criteria.getMoopMax() != null) {
            filteredPlans = filteredPlans.stream()
                    .filter(p -> p.getMaxOutOfPocketValue() != null
                            && p.getMaxOutOfPocketValue() <= criteria.getMoopMax())
                    .collect(Collectors.toList());
        }

        log.info("Filtered to {} plans", filteredPlans.size());
        return filteredPlans;
    }

    public StateMarketplace findMarketplaceByZipCode(String zipCode) {
        return StateMarketplace.findByStateCode(zipCode);
    }
}