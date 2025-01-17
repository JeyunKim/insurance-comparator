package com.insurance.model.domain;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class InsurancePlanDetails {
    private Plan plan;
    private String planName;
    private String insuranceCompany;

    // Basic getters and setters
    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getInsuranceCompany() {
        return insuranceCompany;
    }

    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }

    // Highlights section
    public String getMonthlyPremium() {
        return String.format("$%.2f", plan.getPremium());
    }

    public String getDeductible() {
        return plan.getDeductibles().stream()
                .filter(d -> "Individual".equals(d.getFamilyCost()))
                .findFirst()
                .map(d -> String.format("$%.2f Individual total", d.getAmount()))
                .orElse("N/A");
    }

    public String getOutOfPocketMaximum() {
        return plan.getMoops().stream()
                .filter(m -> "Individual".equals(m.getFamilyCost()))
                .findFirst()
                .map(m -> String.format("$%.2f Individual total", m.getAmount()))
                .orElse("N/A");
    }

    public boolean isHsaEligible() {
        return plan.isHsaEligible();
    }

    public boolean isReferralRequired() {
        return plan.isSpecialistReferralRequired();
    }

    public boolean isRx3moMailOrder() {
        return plan.isRx3moMailOrder();
    }

    // URLs getters
    public String getFormularyUrl() {
        return plan.getFormularyUrl();
    }

    public String getNetworkUrl() {
        return plan.getNetworkUrl();
    }

    // Medical Care methods
    private String getBenefitCost(String benefitType) {
        return plan.getBenefits().stream()
                .filter(b -> benefitType.equals(b.getType()))
                .findFirst()
                .map(b -> {
                    if (!b.isCovered() || b.getCostSharings() == null || b.getCostSharings().isEmpty()) {
                        return "Benefit not covered";
                    }

                    return b.getCostSharings().stream()
                            .map(cs -> {
                                String display = cs.getNetworkTier() + ": " + cs.getDisplayString();
                                if ("BBD".equals(cs.getBenefitBeforeDeductible())) {
                                    display += " from day 1";
                                }
                                return display;
                            })
                            .collect(Collectors.joining(" | "));
                })
                .orElse("Benefit not covered");
    }

    public String getPrimaryCare() {
        return getBenefitCost("PRIMARY_CARE_VISIT_TO_TREAT_AN_INJURY_OR_ILLNESS");
    }

    public String getSpecialist() {
        return getBenefitCost("SPECIALIST_VISIT");
    }

    public String getXraysAndDiagnosticImaging() {
        return getBenefitCost("IMAGING_DIAGNOSTIC");
    }

    public String getLaboratoryServices() {
        return getBenefitCost("LABORATORY_OUTPATIENT_AND_PROFESSIONAL_SERVICES");
    }

    public String getOutpatientFacility() {
        return getBenefitCost("OUTPATIENT_FACILITY_FEE_EG_AMBULATORY_SURGERY_CENTER");
    }

    public String getOutpatientProfessionalServices() {
        return getBenefitCost("OUTPATIENT_SURGERY_PHYSICIAN_SURGICAL_SERVICES");
    }

    public String getHearingAids() {
        return getBenefitCost("HEARING_AIDS");
    }

    public String getRoutineEyeExamAdults() {
        return getBenefitCost("ROUTINE_EYE_EXAM_FOR_ADULTS");
    }

    public String getRoutineEyeExamChildren() {
        return getBenefitCost("ROUTINE_EYE_EXAM_FOR_CHILDREN");
    }

    public String getEyeglassesChildren() {
        return getBenefitCost("EYEGLASSES_FOR_CHILDREN");
    }

    // Prescription Drugs methods
    public String getGenericDrugs() {
        return getBenefitCost("GENERIC_DRUGS");
    }

    public String getPreferredBrandDrugs() {
        return getBenefitCost("PREFERRED_BRAND_DRUGS");
    }

    public String getNonPreferredBrandDrugs() {
        return getBenefitCost("NON_PREFERRED_BRAND_DRUGS");
    }

    public String getSpecialtyDrugs() {
        return getBenefitCost("SPECIALTY_DRUGS");
    }

    // Hospital Services methods
    public String getUrgentCare() {
        return getBenefitCost("URGENT_CARE_CENTERS_OR_FACILITIES");
    }

    public String getEmergencyRoom() {
        return getBenefitCost("EMERGENCY_ROOM_SERVICES");
    }

    public String getInpatientDoctorServices() {
        return getBenefitCost("INPATIENT_PHYSICIAN_AND_SURGICAL_SERVICES");
    }

    public String getInpatientHospitalServices() {
        return getBenefitCost("INPATIENT_HOSPITAL_SERVICES_EG_HOSPITAL_STAY");
    }

    // Dental Coverage methods
    public String getAdultRoutineDental() {
        return getBenefitCost("DENTAL_ADULT_ROUTINE");
    }

    public String getAdultBasicDental() {
        return getBenefitCost("DENTAL_ADULT_BASIC");
    }

    public String getAdultMajorDental() {
        return getBenefitCost("DENTAL_ADULT_MAJOR");
    }

    public String getAdultOrthodontia() {
        return getBenefitCost("DENTAL_ADULT_ORTHODONTIA");
    }

    public String getChildCheckup() {
        return getBenefitCost("DENTAL_CHECK_UP_FOR_CHILDREN");
    }

    public String getChildBasicDental() {
        return getBenefitCost("DENTAL_CHILD_BASIC");
    }

    public String getChildMajorDental() {
        return getBenefitCost("DENTAL_CHILD_MAJOR");
    }

    public String getChildOrthodontia() {
        return getBenefitCost("DENTAL_CHILD_ORTHODONTIA");
    }

    // Other Services methods
    public String getAcupuncture() {
        return getBenefitCost("ACUPUNCTURE");
    }

    public String getChiropracticCare() {
        return getBenefitCost("CHIROPRACTIC_CARE");
    }

    public String getInfertilityTreatment() {
        return getBenefitCost("INFERTILITY_TREATMENT");
    }

    public String getMentalHealthOutpatient() {
        return getBenefitCost("MENTAL_BEHAVIORAL_HEALTH_OUTPATIENT_SERVICES");
    }

    public String getMentalHealthInpatient() {
        return getBenefitCost("MENTAL_BEHAVIORAL_HEALTH_INPATIENT_SERVICES");
    }

    public String getHabilitativeServices() {
        return getBenefitCost("HABILITATION_SERVICES");
    }

    public String getBariatricSurgery() {
        return getBenefitCost("BARIATRIC_SURGERY");
    }

    public String getRehabilitationServices() {
        return getBenefitCost("OUTPATIENT_REHABILITATION_SERVICES");
    }

    public String getSkilledNursingCare() {
        return getBenefitCost("SKILLED_NURSING_FACILITY");
    }

    public String getPrivateDutyNursing() {
        return getBenefitCost("PRIVATE_DUTY_NURSING");
    }

    // Inner classes (Plan, Deductible, Moop, etc.)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Plan {
        private String id;
        private String name;
        private double premium;
        @JsonProperty("premium_w_credit")
        private double premiumWCredit;
        @JsonProperty("ehb_premium")
        private double ehbPremium;
        @JsonProperty("pediatric_ehb_premium")
        private double pediatricEhbPremium;
        @JsonProperty("aptc_eligible_premium")
        private double aptcEligiblePremium;
        @JsonProperty("metal_level")
        private String metalLevel;
        private String type;
        private String state;
        @JsonProperty("design_type")
        private String designType;
        @JsonProperty("is_standardized_plan")
        private boolean isStandardizedPlan;
        private List<Deductible> deductibles;
        private List<Moop> moops;
        private List<Benefit> benefits;
        @JsonProperty("benefits_url")
        private String benefitsUrl;
        @JsonProperty("brochure_url")
        private String brochureUrl;
        @JsonProperty("formulary_url")
        private String formularyUrl;
        @JsonProperty("network_url")
        private String networkUrl;
        @JsonProperty("hsa_eligible")
        private boolean hsaEligible;
        @JsonProperty("specialist_referral_required")
        private boolean specialistReferralRequired;
        @JsonProperty("rx3mo_mail_order")
        private boolean rx3moMailOrder;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Deductible {
        private String type;
        private double amount;
        @JsonProperty("family_cost")
        private String familyCost;
        @JsonProperty("csr")
        private String csr;
        @JsonProperty("network_tier")
        private String networkTier;
        @JsonProperty("individual")
        private String individual;
        @JsonProperty("family")
        private String family;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Moop {
        private String type;
        private double amount;
        @JsonProperty("family_cost")
        private String familyCost;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Benefit {
        private String type;
        private String name;
        private boolean covered;
        @JsonProperty("cost_sharings")
        private List<CostSharing> costSharings;
        private String explanation;
        private String exclusions;
        @JsonProperty("has_limits")
        private boolean hasLimits;
        @JsonProperty("limit_unit")
        private String limitUnit;
        @JsonProperty("limit_quantity")
        private int limitQuantity;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CostSharing {
        @JsonProperty("coinsurance_rate")
        private double coinsuranceRate;
        @JsonProperty("coinsurance_options")
        private String coinsuranceOptions;
        @JsonProperty("copay_amount")
        private double copayAmount;
        @JsonProperty("copay_options")
        private String copayOptions;
        @JsonProperty("network_tier")
        private String networkTier;
        @JsonProperty("display_string")
        private String displayString;
        @JsonProperty("benefit_before_deductible")
        private String benefitBeforeDeductible;
        @JsonProperty("csr")
        private String csr;
    }
}
