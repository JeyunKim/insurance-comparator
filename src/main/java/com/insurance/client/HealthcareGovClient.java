package com.insurance.client;

import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.dto.InsurancePlansResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;
import com.insurance.service.LocationService;
import java.util.ArrayList;
import org.springframework.web.client.HttpClientErrorException;
import com.insurance.model.domain.StateMarketplace;

@Slf4j
@Component
public class HealthcareGovClient {

    private final RestTemplate restTemplate;
    private final LocationService locationService;
    private final String baseUrl;
    private final String apiKey;

    public HealthcareGovClient(
            RestTemplate restTemplate,
            LocationService locationService,
            @Value("${healthcare.gov.api.base-url:https://marketplace.api.healthcare.gov}") String baseUrl,
            @Value("${healthcare.gov.api.key}") String apiKey) {
        this.restTemplate = restTemplate;
        this.locationService = locationService;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        log.info("HealthcareGovClient initialized with baseUrl: {}", baseUrl);
        log.info("API Key present: {}", apiKey != null && !apiKey.isEmpty());
    }

    public InsurancePlan[] getInsurancePlans(String zipCode, int year) {
        String url = baseUrl + "/api/v1/plans/search";
        LocationService.LocationInfo locationInfo = locationService.getLocationInfo(zipCode);

        // Validate location info
        if (locationInfo.getState() == null || locationInfo.getState().isEmpty() ||
                locationInfo.getCountyFips() == null || locationInfo.getCountyFips().isEmpty()) {
            throw new IllegalArgumentException(
                    "Location information not available for ZIP code " + zipCode +
                            ". Please try a different ZIP code in our service area.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create request body with all required fields and default values
        Map<String, Object> requestBody = new HashMap<>();

        // Household information
        requestBody.put("household", new HashMap<String, Object>() {
            {
                put("income", 52000.0); // Double type for income
                put("has_married_couple", false); // Boolean type
                put("people", new ArrayList<>() {
                    {
                        add(new HashMap<String, Object>() {
                            {
                                put("age", 35);
                                put("aptc_eligible", true); // Boolean type
                                put("gender", "Female");
                                put("uses_tobacco", false); // Boolean type
                                put("relationship", "Self");
                            }
                        });
                    }
                });
                put("effective_date", "2024-02-01");
            }
        });

        // Market and enrollment period
        requestBody.put("market", "Individual");
        requestBody.put("enrollment_period", "Special");

        // Location information
        requestBody.put("place", new HashMap<String, Object>() {
            {
                put("zipcode", zipCode);
                put("state", locationInfo.getState());
                put("county", locationInfo.getCounty());
                put("countyfips", locationInfo.getCountyFips());
            }
        });

        // Coverage and preferences
        requestBody.put("year", year);
        requestBody.put("coverage", "Medical");
        requestBody.put("dental_coverage", false); // Boolean type
        requestBody.put("catastrophic", true); // Boolean type
        requestBody.put("order_by", "premium");
        requestBody.put("limit", 50);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.debug("Requesting insurance plans with URL: {} and body: {}", url, requestBody);
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class);

            // Split the entire response into lines for logging
            String[] lines = rawResponse.getBody().split(",");
            for (String line : lines) {
                if (line.contains("deductible") || line.contains("moop") || line.contains("out_of_pocket")) {
                    log.debug("Found relevant line: {}", line);
                }
            }

            ResponseEntity<InsurancePlansResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    InsurancePlansResponse.class);
            log.debug("Parsed API Response: {}", response.getBody());
            return response.getBody().getPlans().toArray(new InsurancePlan[0]);
        } catch (HttpClientErrorException.BadRequest e) {
            // Handle 400 Bad Request error
            if (e.getMessage().contains("invalid zipcode")) {
                log.error("Location information not available for ZIP code: {}", zipCode);
                throw new IllegalArgumentException(
                        "Sorry, we currently don't have coverage information for ZIP code " + zipCode +
                                ". Please try a different ZIP code in our service area.");
            } else if (e.getMessage().contains("not a valid marketplace state")) {
                String state = locationInfo.getState();
                StateMarketplace stateMarketplace = StateMarketplace.findByStateCode(state);
                if (stateMarketplace != null) {
                    throw new IllegalArgumentException(
                            String.format("%s has its own Marketplace. %s is your state's Marketplace. " +
                                    "Visit <a href='%s' target='_blank'>%s's website</a>.",
                                    stateMarketplace.getStateName(),
                                    stateMarketplace.getMarketplaceName(),
                                    stateMarketplace.getWebsiteUrl(),
                                    stateMarketplace.getStateName()));
                }
                log.error("State is not supported: {}", state);
                throw new IllegalArgumentException(
                        "Sorry, " + state + " is not currently supported by the Healthcare Marketplace. " +
                                "Please try a ZIP code in one of our supported states (e.g., Arizona).");
            }
            log.error("Error fetching insurance plans for ZIP {}: {}", zipCode, e.getMessage());
            throw new RuntimeException("Failed to fetch insurance plans: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching insurance plans for ZIP {}: {}", zipCode, e.getMessage());
            throw new RuntimeException("Failed to fetch insurance plans", e);
        }
    }
}