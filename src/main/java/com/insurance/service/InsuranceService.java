package com.insurance.service;

import java.util.*;
import java.util.stream.Collectors;

import com.insurance.client.HealthcareGovClient;
import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.dto.PlanSearchCriteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

        public List<InsurancePlan> getInsurancePlans(String zipCode, int year, PlanSearchCriteria criteria, int page,
                        int size, String sort) {
                // Get plans from the external API
                InsurancePlan[] plans = healthcareGovClient.getInsurancePlans(zipCode, year);
                log.info("Total plans fetched: {}", plans.length);

                // Convert the array to a list for further filtering
                List<InsurancePlan> filteredPlans = Arrays.asList(plans);

                if (criteria == null) {
                        return filteredPlans;
                }

                log.info("Total plans before filtering: {}", filteredPlans.size());

                // Apply filtering based on search criteria
                filteredPlans = filteredPlans.stream()
                                .filter(plan -> filterByMetalLevel(plan, criteria))
                                .filter(plan -> filterByInsuranceCompany(plan, criteria))
                                .filter(plan -> filterByPlanType(plan, criteria))
                                .filter(plan -> filterByPremiumRange(plan, criteria))
                                .filter(plan -> filterByDeductibleRange(plan, criteria))
                                .filter(plan -> filterByMaxOutOfPocketRange(plan, criteria))
                                .collect(Collectors.toList());

                log.info("Filtered to {} plans", filteredPlans.size());

                // Apply pagination and sorting
                return applyPaginationAndSorting(filteredPlans, page, size, sort);
        }

        private List<InsurancePlan> applyPaginationAndSorting(List<InsurancePlan> plans, int page, int size,
                        String sort) {
                if (StringUtils.hasText(sort)) {
                        plans = sortPlans(plans, sort);
                }

                int totalPlans = plans.size();
                int totalPages = (int) Math.ceil((double) totalPlans / size);

                // Ensure the page is within a valid range
                page = Math.max(0, Math.min(page, Math.max(totalPages - 1, 0)));

                int start = page * size;
                int end = Math.min(start + size, totalPlans);

                // Get the paginated plans
                return totalPlans > 0 ? plans.subList(start, end) : plans;
        }

        private List<InsurancePlan> sortPlans(List<InsurancePlan> plans, String sort) {
                String[] sortParams = sort.split(",");
                String field = sortParams[0];
                boolean ascending = sortParams.length > 1 && "asc".equals(sortParams[1]);

                Comparator<InsurancePlan> comparator = createComparator(field);
                if (comparator != null) {
                        if (!ascending) {
                                comparator = comparator.reversed();
                        }
                        plans.sort(comparator);
                }

                return plans;
        }

        private Comparator<InsurancePlan> createComparator(String field) {
                return switch (field) {
                        case "name" -> Comparator.comparing(
                                        InsurancePlan::getName,
                                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                        case "insuranceCompany" -> Comparator.comparing(
                                        InsurancePlan::getInsuranceCompany,
                                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                        case "premiumValue" -> Comparator.comparing(
                                        InsurancePlan::getPremiumValue,
                                        Comparator.nullsLast(Double::compareTo));
                        case "deductibleValue" -> Comparator.comparing(
                                        InsurancePlan::getDeductibleValue,
                                        Comparator.nullsLast(Double::compareTo));
                        case "maxOutOfPocketValue" -> Comparator.comparing(
                                        InsurancePlan::getMaxOutOfPocketValue,
                                        Comparator.nullsLast(Double::compareTo));
                        default -> null;
                };
        }

        // Filter criteria methods...
        private boolean filterByMetalLevel(InsurancePlan plan, PlanSearchCriteria criteria) {
                return StringUtils.isEmpty(criteria.getMetalLevel())
                                || plan.getMetalLevel().equalsIgnoreCase(criteria.getMetalLevel());
        }

        private boolean filterByInsuranceCompany(InsurancePlan plan, PlanSearchCriteria criteria) {
                return StringUtils.isEmpty(criteria.getInsuranceCompany()) || plan.getInsuranceCompany().toLowerCase()
                                .contains(criteria.getInsuranceCompany().toLowerCase());
        }

        private boolean filterByPlanType(InsurancePlan plan, PlanSearchCriteria criteria) {
                return StringUtils.isEmpty(criteria.getPlanType()) || plan.getPlanType().equals(criteria.getPlanType());
        }

        private boolean filterByPremiumRange(InsurancePlan plan, PlanSearchCriteria criteria) {
                return (criteria.getPremiumMin() == null || (plan.getPremiumValue() != null
                                && plan.getPremiumValue() >= criteria.getPremiumMin())) &&
                                (criteria.getPremiumMax() == null || (plan.getPremiumValue() != null
                                                && plan.getPremiumValue() <= criteria.getPremiumMax()));
        }

        private boolean filterByDeductibleRange(InsurancePlan plan, PlanSearchCriteria criteria) {
                return (criteria.getDeductibleMin() == null || (plan.getDeductibleValue() != null
                                && plan.getDeductibleValue() >= criteria.getDeductibleMin())) &&
                                (criteria.getDeductibleMax() == null || (plan.getDeductibleValue() != null
                                                && plan.getDeductibleValue() <= criteria.getDeductibleMax()));
        }

        private boolean filterByMaxOutOfPocketRange(InsurancePlan plan, PlanSearchCriteria criteria) {
                return (criteria.getMoopMin() == null || (plan.getMaxOutOfPocketValue() != null
                                && plan.getMaxOutOfPocketValue() >= criteria.getMoopMin())) &&
                                (criteria.getMoopMax() == null || (plan.getMaxOutOfPocketValue() != null
                                                && plan.getMaxOutOfPocketValue() <= criteria.getMoopMax()));
        }

        public int getTotalPlansCount(String zipCode, int year, PlanSearchCriteria searchCriteria) {
                List<InsurancePlan> allPlans = getInsurancePlans(zipCode, year, searchCriteria, 0, Integer.MAX_VALUE,
                                null);
                return allPlans.size();
        }
}