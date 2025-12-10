package com.intellisix.service_request_management.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.intellisix.service_request_management.model.Notification;

/**
 * Handles JSON file IO for notifications.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/stroage/NotificationFilePersistence.java
 */
@Component
public class NotificationFilePersistence {

    private static final String FILE_PATH = "notifications.json";

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public List<Notification> loadAll() {
        List<Notification> list = new ArrayList<>();

        try {
            if (!Files.exists(Path.of(FILE_PATH))) {
                Files.writeString(Path.of(FILE_PATH), "[]", StandardCharsets.UTF_8);
                return list;
            }

            String all = Files.readString(Path.of(FILE_PATH), StandardCharsets.UTF_8);
            String trimmed = all.trim();

            if (trimmed.equals("[]") || trimmed.isEmpty()) {
                return list;
            }

            List<String> objs = splitTopLevelObjects(all);
            for (String o : objs) {

                String id = extractFieldValue(o, "id");
                String recipientId = extractFieldValue(o, "userId");
                String title = extractFieldValue(o, "title"); // <-- FIX: Extract the new 'title' field
                String message = extractFieldValue(o, "message");

                String createdAtStr = extractFieldValue(o, "timestamp");

                String isReadStr = extractFieldValue(o, "isRead");
                boolean isRead = Boolean.parseBoolean(isReadStr);

                LocalDateTime createdAt = parseDateTime(createdAtStr);

                if (id != null && recipientId != null && message != null && createdAt != null) {
                    // FIX: Use the 6-argument constructor: (id, recipientId, title, message, createdAt, isRead)
                    list.add(new Notification(id, recipientId, title, message, createdAt, isRead));
                }
            }

        } catch (IOException e) {
            System.err.println("Error loading notifications: " + e.getMessage());
        }

        return list;
    }

    public void saveAll(List<Notification> notifications) {
        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);

            json.append("  {\n");

            json.append("    \"id\": \"").append(n.getId()).append("\",\n");
            json.append("    \"userId\": \"").append(n.getRecipientId()).append("\",\n");
            json.append("    \"title\": \"").append(escapeJson(n.getTitle())).append("\",\n"); // <-- FIX: Persist the new 'title' field
            json.append("    \"message\": \"").append(escapeJson(n.getMessage())).append("\",\n");

            String timestamp = n.getCreatedAt() != null
                    ? n.getCreatedAt().format(ISO_FORMATTER)
                    : "null";

            json.append("    \"timestamp\": \"").append(timestamp).append("\",\n");

            json.append("    \"isRead\": \"").append(n.isRead()).append("\"\n");

            json.append("  }");

            if (i < notifications.size() - 1) json.append(",");
            json.append("\n");
        }

        json.append("]");

        try {
            Files.writeString(Path.of(FILE_PATH), json.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error saving notifications JSON: " + e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(String dateString) {
        if (dateString == null || dateString.equalsIgnoreCase("null")) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Failed to parse date: " + dateString + " Reason: " + e.getMessage());
            return null;
        }
    }

    private String extractFieldValue(String obj, String key) {
        String fieldNameQuoted = "\"" + key + "\"";

        Pattern p = Pattern.compile(Pattern.quote(fieldNameQuoted) + ":\\s*\"(.*?)\"");
        Matcher m = p.matcher(obj);
        if (m.find()) return m.group(1);

        p = Pattern.compile(Pattern.quote(fieldNameQuoted) + ":\\s*([^,}\\n]+)");
        m = p.matcher(obj);
        if (m.find()) {
            String value = m.group(1).trim();
            return "null".equalsIgnoreCase(value) ? null : value;
        }

        return null;
    }

    private List<String> splitTopLevelObjects(String jsonArrayString) {
        List<String> objects = new ArrayList<>();
        String trimmed = jsonArrayString.trim();

        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String content = trimmed.substring(1, trimmed.length() - 1).trim();
            if (content.isEmpty()) return objects;

            int braceLevel = 0;
            int start = 0;

            for (int i = 0; i < content.length(); i++) {
                char c = content.charAt(i);

                if (c == '{') braceLevel++;
                else if (c == '}') {
                    braceLevel--;
                    if (braceLevel == 0) {
                        objects.add(content.substring(start, i + 1).trim());
                        start = i + 1;
                    }
                } else if (c == ',' && braceLevel == 0) {
                    start = i + 1;
                }
            }
        }

        return objects;
    }

    private static String escapeJson(String s) {
        return s == null ? "" : s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}