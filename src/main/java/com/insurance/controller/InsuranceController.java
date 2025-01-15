package com.insurance.controller;

import com.insurance.model.domain.InsurancePlan;
import com.insurance.service.InsuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class InsuranceController {
    private final InsuranceService insuranceService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/plans")
    public String getPlans(@RequestParam String zipCode, Model model) {
        try {
            List<InsurancePlan> plans = insuranceService.getInsurancePlans(zipCode, LocalDate.now().getYear());
            model.addAttribute("plans", plans);
            return "plans";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred while fetching insurance plans. Please try again later.");
            return "index";
        }
    }
}