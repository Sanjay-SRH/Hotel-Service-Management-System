package com.intellisix.service_request_management.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Review model + JSON persistence for customer reviews.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/model/Review.java
 */
public class Review {

    private static int nextReviewIndex = 1;

    // FIX: Changed file name from "reviews_archive.json" to "reviews.json"
    private static final String ARCHIVE_FILE = "reviews.json";

    private String request_id;
    private String rating;
    private String review;
    private boolean taken = false;

    private Review() {
        this.rating = "0";
        this.request_id = " ";
        this.review = " ";
        this.taken = false;
    }

    public Review(String request_id, String rating, String review, boolean taken) {
        this.request_id = request_id;
        this.rating = rating;
        this.review = review;
        this.taken = taken;
    }

    // --- Getters and Setters ---
    public String getReq_id() { return this.request_id; }
    public void setReq_id(String req_id) { this.request_id = req_id; }
    public String getReview() { return this.review; }
    public void setReview(String review) { this.review = review; }
    public String getRating() { return this.rating; }
    public String getReviewMessage() { return this.review; } // <-- ADDED THIS LINE
    public void setRating(String rating) { this.rating = rating; }
    public boolean isTaken() { return this.taken; }
    public void setTaken(boolean taken) { this.taken = taken; }


    // --- Persistence Logic ---

    public static List<Review> readAllReviewsFromArchive() {
        List<Review> list = new ArrayList<>();
        File file = new File(ARCHIVE_FILE);

        if (!file.exists() || file.length() == 0) {
            return list;
        }

        try {
            String content = new String(Files.readAllBytes(Paths.get(ARCHIVE_FILE)));
            content = content.trim();

            if (content.startsWith("[")) content = content.substring(1);
            if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

            String[] objects = content.split("},\\s*");

            for (String objStr : objects) {
                objStr = objStr.trim();
                if (objStr.isEmpty()) continue;
                if (!objStr.endsWith("}")) objStr += "}";

                String req_id = extractFieldValue(objStr, "request_id");
                String rating = extractFieldValue(objStr, "rating");
                String reviewMsg = extractFieldValue(objStr, "review");
                String takenStr = extractFieldValue(objStr, "taken");

                if ("null".equals(req_id) || "null".equals(rating)) continue;

                boolean taken = "true".equalsIgnoreCase(takenStr);

                list.add(new Review(req_id, rating, reviewMsg, taken));
            }
        } catch (IOException e) {
            System.err.println("Error loading review archive: " + e.getMessage());
        }
        return list;
    }

    private static String extractFieldValue(String json, String key) {
        String search = "\"" + key + "\": \"";
        int start = json.indexOf(search);
        if (start == -1) {
            // Check for unquoted boolean values: "taken": true/false
            if (json.contains("\"" + key + "\": true")) return "true";
            if (json.contains("\"" + key + "\": false")) return "false";
            return "null";
        }
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "null";
        // Handle escaped quotes in the review message itself
        String value = json.substring(start, end);
        return value.replace("\\\"", "\"");
    }

    private static void writeAllReviewsToArchive(List<Review> reviews) {
        StringBuilder json = new StringBuilder();
        json.append("[\n");

        try {
            for (int i = 0; i < reviews.size(); i++) {
                Review r = reviews.get(i);
                json.append("  {\n");
                json.append("    \"request_id\": \"").append(r.getReq_id()).append("\",\n");
                json.append("    \"rating\": \"").append(r.getRating()).append("\",\n");

                // Escape internal double quotes in the review message
                String safeReview = r.getReview() != null ? r.getReview().replace("\"", "\\\"") : "";
                json.append("    \"review\": \"").append(safeReview).append("\",\n");

                json.append("    \"taken\": ").append(r.isTaken()).append("\n");
                json.append("  }");
                if (i < reviews.size() - 1) json.append(",");
                json.append("\n");
            }

            json.append("]");

            try (FileWriter writer = new FileWriter(ARCHIVE_FILE)) {
                writer.write(json.toString());
            }
        } catch (IOException e) {
            System.err.println("Error saving review archive: " + e.getMessage());
        }
    }

    // --- Logic for Customer Dashboard ---

    /**
     * Checks if a review has already been submitted for a given request ID.
     * @param requestId The ID of the service request.
     * @return true if a review exists, false otherwise.
     */
    public static boolean isReviewSubmitted(String requestId) {
        List<Review> reviews = readAllReviewsFromArchive();
        return reviews.stream()
                .anyMatch(r -> r.getReq_id() != null && r.getReq_id().equalsIgnoreCase(requestId));
    }

    /**
     * Creates a new review and persists it to the file.
     * @param requestId The ID of the service request being reviewed.
     * @param rating The numerical rating (as a String).
     * @param reviewMessage The review text.
     */
    public static void submitReview(String requestId, String rating, String reviewMessage) {
        Review newReview = new Review(requestId, rating, reviewMessage, true);

        List<Review> reviews = readAllReviewsFromArchive();
        reviews.add(newReview);

        writeAllReviewsToArchive(reviews);
    }

    // --- Display Methods ---

    public void display() {
        System.out.println("Request ID: " + this.request_id +
                ", Rating: " + this.rating + "/5" +
                ", Review: \"" + this.review + "\"");
    }

    public static void viewAllReviews() {
        System.out.println("\n==================================");
        System.out.println("         SAVED CUSTOMER REVIEWS   ");
        System.out.println("==================================");

        List<Review> reviews = readAllReviewsFromArchive();

        if (reviews.isEmpty()) {
            System.out.println("No reviews found in the archive.");
        } else {
            int index = 1;
            for (Review r : reviews) {
                System.out.print("Review #" + index + ": ");
                r.display();
                index++;
            }
        }
        System.out.println("==================================");
    }

    public static void initialize() {
        // Just loads the reviews once to potentially set up an in-memory index
        // but currently just performs a read operation.
        readAllReviewsFromArchive();
    }
}