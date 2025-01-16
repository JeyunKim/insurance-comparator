package com.insurance.controller;

import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.domain.StateMarketplace;
import com.insurance.model.dto.PlanSearchCriteria;
import com.insurance.service.InsuranceService;
import com.insurance.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Comparator;
import java.time.Year;
import java.util.Arrays;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InsuranceController {
    private final InsuranceService insuranceService;
    private final LocationService locationService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/plans")
    public String getPlans(@RequestParam String zipCode,
            @RequestParam(defaultValue = "0") int page,
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

            return processPlansRequest(zipCode, page, size, sort, searchCriteria, model);
        } catch (Exception e) {
            log.error("Error fetching insurance plans: {}", e.getMessage(), e);
            model.addAttribute("error", "An error occurred while fetching insurance plans. Please try again.");
            return "index";
        }
    }

    private String processPlansRequest(String zipCode, int page, int size,
            String sort, PlanSearchCriteria searchCriteria, Model model) {
        List<InsurancePlan> allPlans = insuranceService.getInsurancePlans(zipCode, Year.now().getValue(),
                searchCriteria);

        if (allPlans.isEmpty()) {
            model.addAttribute("error", "No plans found for this ZIP code.");
            return "index";
        }

        if (StringUtils.hasText(sort)) {
            sortPlans(allPlans, sort);
        }

        addPaginationAttributes(model, allPlans, page, size);
        model.addAttribute("searchCriteria", searchCriteria);
        model.addAttribute("sort", sort);

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

    private void addPaginationAttributes(Model model, List<InsurancePlan> allPlans, int page, int size) {
        int totalPlans = allPlans.size();
        int totalPages = (int) Math.ceil((double) totalPlans / size);
        int start = Math.min(page * size, totalPlans);
        int end = Math.min(start + size, totalPlans);

        model.addAttribute("plans", allPlans.subList(start, end));
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
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
            model.addAttribute("htmlError", true); // Thymeleaf에서 HTML 렌더링을 위한 플래그
            return "index";
        } catch (Exception e) {
            log.error("Error in handleStateMarketplaceError for ZIP code {}: {}", zipCode, e.getMessage());
            model.addAttribute("error", "An error occurred. Please try again.");
            return "index";
        }
    }
}