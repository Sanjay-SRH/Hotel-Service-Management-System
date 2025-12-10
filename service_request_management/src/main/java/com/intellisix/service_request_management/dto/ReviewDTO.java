package com.intellisix.service_request_management.dto;

public class ReviewDTO {
    private String requestId;
    private String rating;
    private String reviewMessage;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getReviewMessage() { return reviewMessage; }
    public void setReviewMessage(String reviewMessage) { this.reviewMessage = reviewMessage; }
}
