package com.insurance.util;

import org.springframework.stereotype.Component;

/**
 * Utility class for formatting insurance plan names and details
 * Provides methods for standardizing and cleaning plan information
 */
@Component
public class PlanNameFormatter {
    public PlanNameFormatter() {
    }

    /**
     * Formats a plan name according to standard conventions
     * 
     * @param planName Raw plan name to format
     * @return Formatted plan name
     */
    public String formatPlanName(String planName) {
        if (planName == null)
            return "";

        // Remove content in parentheses
        planName = planName.replaceAll("\\(.*?\\)", "").trim();

        // Remove content after special characters
        String[] stopChars = { ":", "+", "$" };
        int firstStop = planName.length();

        for (String stopChar : stopChars) {
            int index = planName.indexOf(stopChar);
            if (index > 0 && index < firstStop) {
                firstStop = index;
            }
        }

        // Extract base name only
        String baseName = planName.substring(0, firstStop).trim();

        // Remove consecutive whitespace
        return baseName.replaceAll("\\s+", " ");
    }
}