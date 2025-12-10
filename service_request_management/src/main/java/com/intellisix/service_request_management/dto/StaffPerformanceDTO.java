package com.intellisix.service_request_management.dto;

public class StaffPerformanceDTO {

    private String staffId;
    private int totalCompletedRequests;
    private int reviewedCompletedRequests;
    private double averageRating; // 0.0 if no reviews

    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }

    public int getTotalCompletedRequests() { return totalCompletedRequests; }
    public void setTotalCompletedRequests(int totalCompletedRequests) {
        this.totalCompletedRequests = totalCompletedRequests;
    }

    public int getReviewedCompletedRequests() { return reviewedCompletedRequests; }
    public void setReviewedCompletedRequests(int reviewedCompletedRequests) {
        this.reviewedCompletedRequests = reviewedCompletedRequests;
    }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
}
