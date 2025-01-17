package com.insurance.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.insurance.client.HealthcareGovClient;
import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.domain.StateMarketplace;
import com.insurance.model.dto.PlanSearchCriteria;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing insurance plan operations
 * Handles business logic for plan searching, filtering, and processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceService {
        private final HealthcareGovClient healthcareGovClient;

        // TODO: [Enhancement] Add search functionality by insurance company name
        // - Create a new method to search plans by insurance company name
        // - Add case-insensitive partial matching for company names
        // - Implement caching for frequently searched insurance companies
        // - Consider adding autocomplete functionality for company names
        // Example method signature:
        // public List<InsurancePlan> searchPlansByInsuranceCompany(String companyName,
        // PlanSearchCriteria criteria)

        /**
         * Retrieves and filters insurance plans based on search criteria
         * 
         * @param zipCode  ZIP code for plan search
         * @param criteria Search criteria for filtering plans
         * @return Filtered list of insurance plans
         */
        public List<InsurancePlan> getInsurancePlans(String zipCode, int year, PlanSearchCriteria criteria) {
                InsurancePlan[] plans = healthcareGovClient.getInsurancePlans(zipCode, year);

                log.info("Total plans fetched: {}", plans.length);

                // Log deductible values for all plans
                for (InsurancePlan plan : plans) {
                        log.info("Plan deductible check - name: {}, company: {}, deductible: {}, type: {}",
                                        plan.getName(),
                                        plan.getInsuranceCompany(),
                                        plan.getDeductible(),
                                        plan.getDeductible() != null ? plan.getDeductible().getClass().getName()
                                                        : "null");
                }

                List<InsurancePlan> filteredPlans = Arrays.asList(plans);

                if (criteria == null) {
                        return filteredPlans;
                }

                log.info("Total plans before filtering: {}", filteredPlans.size());

                // Filter plans by Metal Level
                if (StringUtils.hasText(criteria.getMetalLevel())) {
                        filteredPlans = filteredPlans.stream()
                                        .filter(plan -> plan.getMetalLevel().equalsIgnoreCase(criteria.getMetalLevel()))
                                        .collect(Collectors.toList());
                }

                // Filter plans by Insurance Company
                if (StringUtils.hasText(criteria.getInsuranceCompany())) {
                        filteredPlans = filteredPlans.stream()
                                        .filter(plan -> plan.getInsuranceCompany().toLowerCase()
                                                        .contains(criteria.getInsuranceCompany().toLowerCase()))
                                        .collect(Collectors.toList());
                }

                // Filter plans by Plan Type
                if (StringUtils.hasText(criteria.getPlanType())) {
                        filteredPlans = filteredPlans.stream()
                                        .filter(plan -> plan.getPlanType().equals(criteria.getPlanType()))
                                        .collect(Collectors.toList());
                }

                // Filter by Premium Range
                if (criteria.getPremiumMin() != null) {
                        filteredPlans = filteredPlans.stream()
                                        .filter(p -> p.getPremiumValue() != null
                                                        && p.getPremiumValue() >= criteria.getPremiumMin())
                                        .collect(Collectors.toList());
                }
                if (criteria.getPremiumMax() != null) {
                        filteredPlans = filteredPlans.stream()
                                        .filter(p -> p.getPremiumValue() != null
                                                        && p.getPremiumValue() <= criteria.getPremiumMax())
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