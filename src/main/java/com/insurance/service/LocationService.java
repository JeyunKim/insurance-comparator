package com.insurance.service;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling location-related operations
 * Manages ZIP code validation and location information retrieval
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {
    private final RestTemplate restTemplate;
    private static final String ZIP_API_URL = "http://api.zippopotam.us/us/";
    private static final String FCC_API_URL = "https://geo.fcc.gov/api/census/area";
    private static final Map<String, String> STATE_CODES = Map.ofEntries(
            Map.entry("Alabama", "AL"),
            Map.entry("Alaska", "AK"),
            Map.entry("Arizona", "AZ"),
            Map.entry("Arkansas", "AR"),
            Map.entry("California", "CA"),
            Map.entry("Colorado", "CO"),
            Map.entry("Connecticut", "CT"),
            Map.entry("Delaware", "DE"),
            Map.entry("Florida", "FL"),
            Map.entry("Georgia", "GA"),
            Map.entry("Hawaii", "HI"),
            Map.entry("Idaho", "ID"),
            Map.entry("Illinois", "IL"),
            Map.entry("Indiana", "IN"),
            Map.entry("Iowa", "IA"),
            Map.entry("Kansas", "KS"),
            Map.entry("Kentucky", "KY"),
            Map.entry("Louisiana", "LA"),
            Map.entry("Maine", "ME"),
            Map.entry("Maryland", "MD"),
            Map.entry("Massachusetts", "MA"),
            Map.entry("Michigan", "MI"),
            Map.entry("Minnesota", "MN"),
            Map.entry("Mississippi", "MS"),
            Map.entry("Missouri", "MO"),
            Map.entry("Montana", "MT"),
            Map.entry("Nebraska", "NE"),
            Map.entry("Nevada", "NV"),
            Map.entry("New Hampshire", "NH"),
            Map.entry("New Jersey", "NJ"),
            Map.entry("New Mexico", "NM"),
            Map.entry("New York", "NY"),
            Map.entry("North Carolina", "NC"),
            Map.entry("North Dakota", "ND"),
            Map.entry("Ohio", "OH"),
            Map.entry("Oklahoma", "OK"),
            Map.entry("Oregon", "OR"),
            Map.entry("Pennsylvania", "PA"),
            Map.entry("Rhode Island", "RI"),
            Map.entry("South Carolina", "SC"),
            Map.entry("South Dakota", "SD"),
            Map.entry("Tennessee", "TN"),
            Map.entry("Texas", "TX"),
            Map.entry("Utah", "UT"),
            Map.entry("Vermont", "VT"),
            Map.entry("Virginia", "VA"),
            Map.entry("Washington", "WA"),
            Map.entry("West Virginia", "WV"),
            Map.entry("Wisconsin", "WI"),
            Map.entry("Wyoming", "WY"));

    /**
     * Retrieves location information for a given ZIP code
     * 
     * @param zipCode ZIP code to look up
     * @return LocationInfo containing state and county information
     * @throws RuntimeException if location information cannot be retrieved
     */
    public LocationInfo getLocationInfo(String zipCode) {
        try {
            // First, get basic location info from Zippopotam API
            String zipUrl = ZIP_API_URL + zipCode;
            log.debug("Requesting Zippopotam API: {}", zipUrl);
            ZipResponse zipResponse = restTemplate.getForObject(zipUrl, ZipResponse.class);
            log.debug("Zippopotam Response: {}", zipResponse);

            if (zipResponse == null || zipResponse.getPlaces() == null || zipResponse.getPlaces().isEmpty()) {
                throw new IllegalArgumentException("No location information found for ZIP code: " + zipCode);
            }

            ZipResponse.Place place = zipResponse.getPlaces().get(0);
            String stateCode = STATE_CODES.getOrDefault(place.getState(), place.getState());

            // Then, get FIPS code from FCC API
            String censusUrl = String.format(
                    "%s?format=json&lat=%s&lon=%s",
                    FCC_API_URL,
                    place.getLatitude(),
                    place.getLongitude());

            log.debug("Requesting FCC API: {}", censusUrl);
            FccResponse fccResponse = restTemplate.getForObject(censusUrl, FccResponse.class);
            log.debug("FCC Response: {}", fccResponse);

            String countyFips = extractCountyFips(fccResponse);
            if (countyFips.isEmpty()) {
                throw new IllegalArgumentException("Could not determine county information for ZIP code: " + zipCode);
            }

            return new LocationInfo(
                    stateCode,
                    place.getPlaceName(),
                    countyFips);
        } catch (Exception e) {
            log.error("Error fetching location info for ZIP {}: {}", zipCode, e.getMessage());
            throw new IllegalArgumentException("Invalid ZIP code: " + zipCode);
        }
    }

    private String extractCountyFips(FccResponse response) {
        if (response != null &&
                response.getResults() != null &&
                !response.getResults().isEmpty()) {
            return response.getResults().get(0).getCountyFips();
        }
        return "";
    }

    @Data
    static class FccResponse {
        @JsonProperty("results")
        private List<Result> results;

        @Data
        static class Result {
            @JsonProperty("county_fips")
            private String countyFips;
        }
    }

    @Data
    static class ZipResponse {
        @JsonProperty("post code")
        private String postCode;
        @JsonProperty("country")
        private String country;
        @JsonProperty("places")
        private List<Place> places;

        @Data
        static class Place {
            @JsonProperty("place name")
            private String placeName;
            @JsonProperty("state")
            private String state;
            @JsonProperty("latitude")
            private String latitude;
            @JsonProperty("longitude")
            private String longitude;
        }
    }

    /**
     * Location information container class
     */
    @Data
    @AllArgsConstructor
    public static class LocationInfo {
        private String state;
        private String county;
        private String countyFips;
    }
}