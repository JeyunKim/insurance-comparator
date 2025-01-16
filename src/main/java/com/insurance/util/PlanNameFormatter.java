package com.insurance.util;

public class PlanNameFormatter {
    private PlanNameFormatter() {
    }

    public static String formatPlanName(String fullName) {
        if (fullName == null)
            return "";

        // 괄호 내용 제거
        fullName = fullName.replaceAll("\\(.*?\\)", "").trim();

        // 특수 문자 이후 내용 제거
        String[] stopChars = { ":", "+", "$" };
        int firstStop = fullName.length();

        for (String stopChar : stopChars) {
            int index = fullName.indexOf(stopChar);
            if (index > 0 && index < firstStop) {
                firstStop = index;
            }
        }

        // 기본 이름만 추출
        String baseName = fullName.substring(0, firstStop).trim();

        // 연속된 공백 제거
        return baseName.replaceAll("\\s+", " ");
    }
}