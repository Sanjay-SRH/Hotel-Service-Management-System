package com.intellisix.service_request_management.service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating unique request IDs.
 * Place in:
 * src/main/java/com/intellisix/service_request_management/service/RequestIdUtil.java
 */
public final class RequestIdUtil {

    private RequestIdUtil() {} // Prevent instantiation

    // Map human-friendly type to 2-letter code used in requestId
    public static String mapTypeToCode(String type) {
        if (type == null) return "GM";
        switch (type.trim().toLowerCase()) {
            case "electrical": return "EL";
            case "plumbing":   return "PL";
            case "food":       return "FD";
            case "laundry":    return "LD";
            default:           return "GM";
        }
    }

    /**
     * Normalize clientId which now includes the room number (e.g., C004-101).
     * If the input is like "C004-101-101", it returns "C004-101".
     */
    public static String normalizeClientId(String clientId) {
        if (clientId == null) return "UNKNOWN";
        String raw = clientId.trim();
        if (raw.isEmpty()) return "UNKNOWN";

        String[] parts = raw.split("-");
        if (parts.length <= 2) return String.join("-", parts).replaceAll("\\s+", "");
        return (parts[0] + "-" + parts[1]).replaceAll("\\s+", "");
    }

    /**
     * Generate request id of the form:
     * <TYPECODE>-<normalizedClientId>-<rnd3>
     * e.g. EL-C001-101-421
     */
    public static String generateId(String serviceType, String clientId) {
        String typeCode = mapTypeToCode(serviceType);
        String normalizedClient = normalizeClientId(clientId);

        // Generate a random 3-digit number
        int randomNum = ThreadLocalRandom.current().nextInt(100, 999);
        String randomStr = String.format("%03d", randomNum);

        return String.format("%s-%s-%s", typeCode, normalizedClient, randomStr);
    }
}
