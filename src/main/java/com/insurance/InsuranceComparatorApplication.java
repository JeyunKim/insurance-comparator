package com.insurance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Insurance Comparator
 * Initializes and configures the application context
 */
@SpringBootApplication
public class InsuranceComparatorApplication {
    /**
     * Application entry point
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(InsuranceComparatorApplication.class, args);
    }
}