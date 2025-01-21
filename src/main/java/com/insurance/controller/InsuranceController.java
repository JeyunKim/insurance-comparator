package com.insurance.controller;

import java.time.Year;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.insurance.client.HealthcareGovClient;
import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.domain.InsurancePlanDetails;
import com.insurance.model.domain.StateMarketplace;
import com.insurance.model.dto.PlanSearchCriteria;
import com.insurance.service.InsuranceService;
import com.insurance.service.LocationService;
import com.insurance.util.PlanNameFormatter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for handling insurance plan related requests
 * Provides endpoints for searching and viewing insurance plans
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class InsuranceController {
    private final InsuranceService insuranceService;
    private final LocationService locationService;
    private final HealthcareGovClient healthcareGovClient;
    private final PlanNameFormatter planNameFormatter;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * Handles the request to get insurance plans based on search criteria
     * 
     * @param zipCode          ZIP code for plan search
     * @param page             Current page number
     * @param size             Number of items per page
     * @param insuranceCompany Insurance company filter
     * @param criteria         Additional search criteria
     * @param model            Spring MVC model
     * @return View name for plan results
     */
    @GetMapping("/plans")
    public String getPlans(@RequestParam String zipCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @ModelAttribute PlanSearchCriteria searchCriteria,
            Model model) {
        try {
            if (!StringUtils.hasText(zipCode)) {
                return "redirect:/";
            }

            LocationService.LocationInfo locationInfo = locationService.getLocationInfo(zipCode);
            String state = locationInfo.getState();

            if (Arrays.stream(StateMarketplace.values()).anyMatch(s -> s.name().equals(state))) {
                return handleStateMarketplaceError(zipCode, model);
            }

            return processPlansRequest(zipCode, page - 1, size, sort, searchCriteria, model);
        } catch (Exception e) {
            log.error("Error fetching insurance plans: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while fetching insurance plans. Please try again.");
            return "index";
        }
    }

    private String processPlansRequest(String zipCode, int page, int size,
            String sort, PlanSearchCriteria searchCriteria, Model model) {
        // Get all plans
        List<InsurancePlan> allPlans = insuranceService.getInsurancePlans(zipCode, Year.now().getValue(),
                searchCriteria, 0, Integer.MAX_VALUE, sort);

        if (allPlans.isEmpty()) {
            model.addAttribute("error", "No plans found for this ZIP code.");
            return "index";
        }

        // Calculate total pages
        int totalPlans = allPlans.size();
        int totalPages = (totalPlans + size - 1) / size;

        // Get paginated subset
        int start = page * size;
        int end = Math.min(start + size, totalPlans);
        List<InsurancePlan> pagedPlans = allPlans.subList(start, end);

        if (StringUtils.hasText(sort)) {
            sortPlans(pagedPlans, sort);
        }

        // Format plan names
        pagedPlans.forEach(plan -> plan.setName(planNameFormatter.formatPlanName(plan.getName())));

        // Add attributes to model
        model.addAttribute("plans", pagedPlans);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalPlans", totalPlans);
        model.addAttribute("searchCriteria", searchCriteria);
        model.addAttribute("sort", sort);

        log.debug("Pagination info - Total plans: {}, Page size: {}, Total pages: {}, Current page: {}",
                totalPlans, size, totalPages, page + 1);

        return "plans";
    }

    private void sortPlans(List<InsurancePlan> plans, String sort) {
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

    private String handleStateMarketplaceError(String zipCode, Model model) {
        try {
            LocationService.LocationInfo locationInfo = locationService.getLocationInfo(zipCode);
            String state = locationInfo.getState();
            StateMarketplace marketplace = StateMarketplace.valueOf(state);
            String message = String.format(
                    "The state %s has its own marketplace. Please visit <a href='%s' target='_blank'>%s</a>",
                    state, marketplace.getWebsiteUrl(), marketplace.getWebsiteUrl());

            model.addAttribute("error", message);
            model.addAttribute("htmlError", true); // Flag for HTML rendering in Thymeleaf
            return "index";
        } catch (Exception e) {
            log.error("Error in handleStateMarketplaceError for ZIP code {}: {}", zipCode, e.getMessage());
            model.addAttribute("error", "An error occurred. Please try again.");
            return "index";
        }
    }

    @GetMapping("/plans/{planId}")
    public String getPlanDetails(@PathVariable String planId,
            @RequestParam String planName,
            @RequestParam String insuranceCompany,
            Model model) {
        InsurancePlanDetails planDetails = healthcareGovClient.getPlanById(planId, planName, insuranceCompany);
        planDetails.setPlanName(planNameFormatter.formatPlanName(planDetails.getPlanName()));
        model.addAttribute("plan", planDetails);
        return "plan-details";
    }

    @GetMapping("/plans/compare")
    public String comparePlans(@RequestParam String ids,
            @RequestParam String names,
            @RequestParam String companies,
            Model model) {
        List<String> planIds = Arrays.asList(ids.split(","));
        List<String> planNames = Arrays.asList(names.split(","));
        List<String> insuranceCompanies = Arrays.asList(companies.split(","));

        List<InsurancePlanDetails> plans = new ArrayList<>();
        for (int i = 0; i < planIds.size(); i++) {
            plans.add(healthcareGovClient.getPlanById(
                    planIds.get(i),
                    planNames.get(i),
                    insuranceCompanies.get(i)));
        }

        model.addAttribute("plans", plans);
        return "compare-plans";
    }
}