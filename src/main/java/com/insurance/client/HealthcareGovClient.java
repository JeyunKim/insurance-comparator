package com.insurance.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.domain.InsurancePlanDetails;
import com.insurance.model.dto.InsurancePlansResponse;
import com.insurance.service.LocationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class HealthcareGovClient {

    private final RestTemplate restTemplate;
    private final LocationService locationService;

    @Value("${healthcare.gov.api.key}")
    private String apiKey;

    @Value("${healthcare.gov.api.base-url}")
    private String baseUrl;

    public InsurancePlan[] getInsurancePlans(String zipCode, int year) {
        LocationService.LocationInfo locationInfo = locationService.getLocationInfo(zipCode);
        return fetchAllPlans(zipCode, year, locationInfo);
    }

    /**
     * Fetches all insurance plans using parallel processing
     * 
     * @param zipCode      ZIP code for the area
     * @param year         Plan year
     * @param locationInfo Location information including state and county
     * @return Array of insurance plans
     */
    private InsurancePlan[] fetchAllPlans(String zipCode, int year, LocationService.LocationInfo locationInfo) {
        // Thread-safe list to store all plans
        List<InsurancePlan> allPlans = Collections.synchronizedList(new ArrayList<>());
        // Set page size to 10 as API returns max 10 plans per request
        int limit = 10;
        int totalPlans = getTotalPlanCount(zipCode, year, locationInfo);

        if (totalPlans == 0) {
            log.warn("No plans found for ZIP: {} and year: {}", zipCode, year);
            return new InsurancePlan[0];
        }

        // Calculate total pages and create thread pool
        int totalPages = (int) Math.ceil((double) totalPlans / limit);
        // Limit concurrent threads to 10 to prevent overwhelming the API
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(totalPages, 10));
        List<CompletableFuture<List<InsurancePlan>>> futures = new ArrayList<>();

        // Create async tasks for each page
        for (int page = 0; page < totalPages; page++) {
            int offset = page * limit;
            CompletableFuture<List<InsurancePlan>> future = CompletableFuture.supplyAsync(() -> {
                return fetchPlansPage(zipCode, year, locationInfo, offset, limit);
            }, executor).exceptionally(throwable -> {
                log.error("Error fetching plans for offset {}: {}", offset, throwable.getMessage());
                return Collections.emptyList();
            });
            futures.add(future);
        }

        try {
            // Wait for all async tasks to complete
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Collect results from all completed futures
            for (CompletableFuture<List<InsurancePlan>> future : futures) {
                List<InsurancePlan> plans = future.get();
                if (plans != null && !plans.isEmpty()) {
                    allPlans.addAll(plans);
                    log.debug("Added {} plans to the result", plans.size());
                }
            }

            log.info("Finished fetching all plans - Total collected: {} (Expected: {})",
                    allPlans.size(), totalPlans);

            if (allPlans.isEmpty()) {
                log.warn("No plans were collected after all requests completed");
            } else if (allPlans.size() < totalPlans) {
                log.warn("Collected fewer plans than expected: {} vs {}", allPlans.size(), totalPlans);
            }

        } catch (Exception e) {
            log.error("Error while fetching plans: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch insurance plans", e);
        } finally {
            executor.shutdown();
        }

        return allPlans.toArray(new InsurancePlan[0]);
    }

    /**
     * Gets the total number of available plans
     * 
     * @return Total number of plans available for the given criteria
     */
    private int getTotalPlanCount(String zipCode, int year, LocationService.LocationInfo locationInfo) {
        Map<String, Object> requestBody = buildRequestBody(zipCode, year, locationInfo, 0, 1);
        HttpEntity<Map<String, Object>> entity = buildHttpEntity(requestBody);

        ResponseEntity<InsurancePlansResponse> response = restTemplate.exchange(
                baseUrl + "/api/v1/plans/search",
                HttpMethod.POST,
                entity,
                InsurancePlansResponse.class);

        InsurancePlansResponse responseBody = response.getBody();
        return responseBody != null ? responseBody.getTotal() : 0;
    }

    /**
     * Fetches a single page of insurance plans with retry mechanism
     * 
     * @param offset Starting position for pagination
     * @param limit  Number of plans to fetch per page
     * @return List of insurance plans for the requested page
     */
    private List<InsurancePlan> fetchPlansPage(String zipCode, int year, LocationService.LocationInfo locationInfo,
            int offset, int limit) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Map<String, Object> requestBody = buildRequestBody(zipCode, year, locationInfo, offset, limit);
                HttpEntity<Map<String, Object>> entity = buildHttpEntity(requestBody);

                log.debug("Fetching plans - offset: {}, limit: {}", offset, limit);

                ResponseEntity<InsurancePlansResponse> response = restTemplate.exchange(
                        baseUrl + "/api/v1/plans/search",
                        HttpMethod.POST,
                        entity,
                        InsurancePlansResponse.class);

                // Process and validate response
                InsurancePlansResponse responseBody = response.getBody();
                if (responseBody != null && responseBody.getPlans() != null && !responseBody.getPlans().isEmpty()) {
                    log.debug("Successfully fetched {} plans for offset: {}",
                            responseBody.getPlans().size(), offset);
                    return responseBody.getPlans();
                }

                log.warn("Received empty or null response for offset: {}", offset);
                return Collections.emptyList();

            } catch (Exception e) {
                retryCount++;
                // Implement exponential backoff for retries
                if (retryCount == maxRetries) {
                    log.error("Failed to fetch plans after {} retries - offset: {}, error: {}",
                            maxRetries, offset, e.getMessage());
                    throw new RuntimeException("Failed to fetch plans for offset: " + offset, e);
                }
                log.warn("Retry #{} - Failed to fetch plans - offset: {}, error: {}",
                        retryCount, offset, e.getMessage());
                try {
                    Thread.sleep(1000 * retryCount);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted while retrying", ie);
                }
            }
        }

        return Collections.emptyList();
    }

    private Map<String, Object> buildRequestBody(String zipCode, int year,
            LocationService.LocationInfo locationInfo, int offset, int limit) {
        Map<String, Object> place = new HashMap<>();
        place.put("zipcode", zipCode.trim());
        place.put("state", locationInfo.getState().trim());
        place.put("countyfips", locationInfo.getCountyFips().trim());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("year", year);
        requestBody.put("place", place);
        requestBody.put("market", "Individual");
        requestBody.put("offset", offset);
        requestBody.put("limit", limit);
        requestBody.put("sort", "premium");

        return requestBody;
    }

    private HttpEntity<Map<String, Object>> buildHttpEntity(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", apiKey);
        return new HttpEntity<>(requestBody, headers);
    }

    public int getTotalPlansCount(String zipCode, int year) {
        try {
            InsurancePlan[] plans = getInsurancePlans(zipCode, year);
            return plans.length;
        } catch (Exception e) {
            log.error("Error fetching total plans count: {}", e.getMessage(), e);
            return 0;
        }
    }

    public InsurancePlanDetails getPlanById(String planId, String planName, String insuranceCompany) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("apikey", apiKey);

            String url = baseUrl + "/api/v1/plans/" + planId;
            log.info("Calling API URL: {}", url);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);

            log.info("API Response for plan {}: {}", planId, response.getBody());

            ObjectMapper mapper = new ObjectMapper();
            InsurancePlanDetails details = mapper.readValue(response.getBody(), InsurancePlanDetails.class);
            details.setPlanName(planName);
            details.setInsuranceCompany(insuranceCompany);

            return details;

        } catch (Exception e) {
            log.error("Error fetching plan details for {}: {} (URL: {})",
                    planId, e.getMessage(), baseUrl + "/api/v1/plans/" + planId);
            throw new RuntimeException("Failed to fetch plan details", e);
        }
    }
}