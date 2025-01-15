package com.insurance.service;

import com.insurance.client.HealthcareGovClient;
import com.insurance.model.domain.InsurancePlan;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final HealthcareGovClient healthcareGovClient;

    public List<InsurancePlan> getInsurancePlans(String zipCode, int year) {
        InsurancePlan[] plans = healthcareGovClient.getInsurancePlans(zipCode, year);
        return Arrays.asList(plans);
    }

    public List<InsurancePlan> searchSpecificPlans(
            String searchTerm,
            String zipCode,
            String planType,
            String metalLevel,
            Double maxMonthlyPremium,
            Double maxDeductible,
            Double maxOutOfPocket,
            Boolean primaryCare,
            Boolean specialist,
            Boolean emergency,
            Boolean prescription,
            Boolean dental,
            Boolean vision) {

        // Get plans for the specified ZIP code
        InsurancePlan[] plans = healthcareGovClient.getInsurancePlans(
                zipCode,
                LocalDate.now().getYear());
        List<InsurancePlan> allPlans = Arrays.asList(plans);

        // Filter by search term
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String searchLower = searchTerm.toLowerCase();
            allPlans = allPlans.stream()
                    .filter(plan -> plan.getName().toLowerCase().contains(searchLower) ||
                            plan.getInsuranceCompany().toLowerCase().contains(searchLower))
                    .toList();
        }

        // Filter by plan type
        if (planType != null && !planType.isEmpty()) {
            allPlans = allPlans.stream()
                    .filter(plan -> plan.getPlanType().equals(planType))
                    .toList();
        }

        // Filter by metal level
        if (metalLevel != null && !metalLevel.isEmpty()) {
            allPlans = allPlans.stream()
                    .filter(plan -> plan.getMetalLevel().equals(metalLevel))
                    .toList();
        }

        // Filter by maximum premium
        if (maxMonthlyPremium != null) {
            allPlans = allPlans.stream()
                    .filter(plan -> plan.getPremiumValue() <= maxMonthlyPremium)
                    .toList();
        }

        return allPlans;
    }
}