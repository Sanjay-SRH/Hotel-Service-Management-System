package com.intellisix.service_request_management.storage;

import com.intellisix.service_request_management.model.ServiceRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Handles JSON file storage and loading for service requests.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/stroage/ServiceRequestFilePersistence.java
 */
@Component
public class ServiceRequestFilePersistence {

    private static final String FILE_PATH = "service_requests.json";

    private static final DateTimeFormatter LEGACY_FORMATTER =
            DateTimeFormatter.ofPattern("E MMM dd HH:mm:ss zzz yyyy", Locale.US);

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<ServiceRequest> loadAll() {
        List<ServiceRequest> list = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(FILE_PATH)));
            content = content.trim();

            if (content.startsWith("[")) content = content.substring(1);
            if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

            String[] objects = content.split("},\\s*");

            for (String objStr : objects) {

                objStr = objStr.trim();
                if (objStr.isEmpty()) continue;
                if (!objStr.endsWith("}")) objStr += "}";

                String reqId = getValue(objStr, "requestId");
                String client = getValue(objStr, "clientId");
                String type = getValue(objStr, "serviceType");
                String desc = getValue(objStr, "description");
                String prio = getValue(objStr, "priority");
                String stat = getValue(objStr, "status");
                String staff = getValue(objStr, "assignedStaffId");
                String notes = getValue(objStr, "completionNotes");
                String createdStr = getValue(objStr, "createdAt");
                String updatedStr = getValue(objStr, "updatedAt");
                String completedStr = getValue(objStr, "completedAt");

                LocalDateTime created = tryParseLocalDateTime(createdStr);
                LocalDateTime updated = tryParseLocalDateTime(updatedStr);
                LocalDateTime completed = tryParseLocalDateTime(completedStr);

                if (created != null && updated != null) {
                    list.add(new ServiceRequest(
                            reqId,      // Corrected from getValue(obj, "requestId")
                            client,     // Corrected from getValue(obj, "clientId")
                            type,       // Corrected from getValue(obj, "serviceType")
                            desc,       // Corrected from getValue(obj, "description")
                            prio,       // Corrected from getValue(obj, "priority")
                            stat,       // Corrected from getValue(obj, "status")
                            staff,      // Corrected from getValue(obj, "assignedStaffId")
                            notes,      // Corrected from getValue(obj, "completionNotes")
                            created,    // Corrected from parseTime(getValue(obj, "createdAt"))
                            updated,    // Corrected from parseTime(getValue(obj, "updatedAt"))
                            completed   // Corrected from parseTime(getValue(obj, "completedAt"))
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading JSON: " + e.getMessage());
        }
        return list;
    }

    private LocalDateTime tryParseLocalDateTime(String dateStr) {
        if (dateStr == null || dateStr.equalsIgnoreCase("null") || dateStr.isEmpty()) {
            return null;
        }

        String parsedStr = dateStr;
        if (parsedStr.startsWith("\"") && parsedStr.endsWith("\"")) {
            parsedStr = parsedStr.substring(1, parsedStr.length() - 1);
        }

        try {
            return LocalDateTime.parse(parsedStr, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(parsedStr, LEGACY_FORMATTER);
            } catch (DateTimeParseException e2) {
                System.err.println("Warning: Could not parse date string '" + parsedStr + "'");
                return null;
            }
        }
    }

    public void saveAll(List<ServiceRequest> requests) {

        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest r = requests.get(i);

            json.append("  {\n");
            json.append("    \"requestId\": \"").append(r.getRequestId()).append("\",\n");
            json.append("    \"clientId\": \"").append(r.getClientId()).append("\",\n");
            json.append("    \"serviceType\": \"").append(r.getServiceType()).append("\",\n");
            json.append("    \"description\": \"").append(r.getDescription()).append("\",\n");
            json.append("    \"priority\": \"").append(r.getPriority()).append("\",\n");
            json.append("    \"status\": \"").append(r.getStatus()).append("\",\n");
            json.append("    \"assignedStaffId\": \"")
                    .append(r.getAssignedStaffId() == null ? "null" : r.getAssignedStaffId()).append("\",\n");
            json.append("    \"completionNotes\": \"")
                    .append(r.getCompletionNotes() == null ? "null" : r.getCompletionNotes()).append("\",\n");

            json.append("    \"createdAt\": \"").append(r.getCreatedAt().format(ISO_FORMATTER)).append("\",\n");
            json.append("    \"updatedAt\": \"").append(r.getUpdatedAt().format(ISO_FORMATTER)).append("\",\n");
            json.append("    \"completedAt\": \"")
                    .append(r.getCompletedAt() == null ? "null" : r.getCompletedAt().format(ISO_FORMATTER))
                    .append("\"\n");

            json.append("  }");

            if (i < requests.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("]");

        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            writer.write(json.toString());
        } catch (IOException e) {
            System.err.println("Error saving JSON: " + e.getMessage());
        }
    }

    private String getValue(String json, String key) {
        String search = "\"" + key + "\": \"";
        int start = json.indexOf(search);

        if (start == -1) {
            if (json.contains("\"" + key + "\": null")) return "null";
            if (json.contains("\"" + key + "\":\"null\"")) return "null";
            return "null";
        }

        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "null";

        return json.substring(start, end);
    }
}