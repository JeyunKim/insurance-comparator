package com.insurance.client;

import com.insurance.model.domain.InsurancePlan;
import com.insurance.model.dto.InsurancePlansResponse;
import com.insurance.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try {
            LocationService.LocationInfo locationInfo = locationService.getLocationInfo(zipCode);
            return fetchAllPlans(zipCode, year, locationInfo);
        } catch (Exception e) {
            log.error("Error fetching insurance plans for ZIP {}: {}", zipCode, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch insurance plans: " + e.getMessage(), e);
        }
    }

    private InsurancePlan[] fetchAllPlans(String zipCode, int year, LocationService.LocationInfo locationInfo) {
        List<InsurancePlan> allPlans = new ArrayList<>();
        int offset = 0;
        int limit = 10;
        boolean hasMore = true;
        int requestCount = 0;
        int totalPlans = 0;

        while (hasMore) {
            requestCount++;
            log.info("Making request #{} for ZIP {} (offset: {}, limit: {})", requestCount, zipCode, offset, limit);

            Map<String, Object> requestBody = buildRequestBody(zipCode, year, locationInfo, offset, limit);
            HttpEntity<Map<String, Object>> entity = buildHttpEntity(requestBody);

            log.debug("Request body: {}", requestBody);

            ResponseEntity<InsurancePlansResponse> response = restTemplate.exchange(
                    baseUrl + "/api/v1/plans/search",
                    HttpMethod.POST,
                    entity,
                    InsurancePlansResponse.class);

            if (response.getBody() != null && response.getBody().getPlans() != null) {
                List<InsurancePlan> plans = response.getBody().getPlans();
                totalPlans = response.getBody().getTotal();
                log.info("Response received - Status: {}, Plans in this batch: {}, Total plans in response: {}",
                        response.getStatusCode(),
                        plans.size(),
                        totalPlans);

                allPlans.addAll(plans);
                hasMore = allPlans.size() < totalPlans;
                offset += limit;
                log.info("Cumulative plans collected: {}", allPlans.size());
            } else {
                log.warn("No plans received in response. Response body: {}", response.getBody());
                hasMore = false;
            }
        }

        log.info("Finished fetching plans - Total requests: {}, Final plan count: {} (Expected: {})",
                requestCount, allPlans.size(), totalPlans);
        return allPlans.toArray(new InsurancePlan[0]);
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
}