package com.intellisix.service_request_management.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.intellisix.service_request_management.dto.StaffPerformanceDTO;
import com.intellisix.service_request_management.model.ServiceRequest;
import com.intellisix.service_request_management.model.Review;
import org.springframework.stereotype.Service;


@Service
public class ReviewService {

    private final ServiceRequestService serviceRequestService;
    private final NotificationService notificationService;
    public StaffPerformanceDTO getStaffPerformance(String staffId) {

        // 1. Fetch all requests handled by this staff
        List<ServiceRequest> staffRequests =
                serviceRequestService.getRequestsByStaff(staffId);

        // 2. Filter only COMPLETED requests
        List<ServiceRequest> completedRequests = staffRequests.stream()
                .filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus()))
                .toList();

        int totalCompleted = completedRequests.size();

        // 3. Load ALL reviews and map them by requestId
        List<Review> allReviews = Review.readAllReviewsFromArchive();

        Map<String, Review> reviewMap = allReviews.stream()
                .collect(Collectors.toMap(
                        Review::getReq_id,    // key → request ID
                        r -> r,
                        (a, b) -> a           // if duplicate exists, keep first
                ));

        int reviewedCount = 0;
        int sumRatings = 0;

        // 4. Calculate ratings for completed requests
        for (ServiceRequest req : completedRequests) {

            Review matchingReview = reviewMap.get(req.getRequestId());

            if (matchingReview != null) {

                reviewedCount++;

                try {
                    int ratingValue = Integer.parseInt(matchingReview.getRating());
                    sumRatings += ratingValue;
                } catch (NumberFormatException e) {
                    // Skip invalid rating values safely
                }
            }
        }

        // 5. Compute average rating
        double avgRating = reviewedCount == 0
                ? 0.0
                : (double) sumRatings / reviewedCount;

        // 6. Return DTO
        StaffPerformanceDTO dto = new StaffPerformanceDTO();

        dto.setStaffId(staffId);
        dto.setTotalCompletedRequests(totalCompleted);
        dto.setReviewedCompletedRequests(reviewedCount);
        dto.setAverageRating(avgRating);

        return dto;
    }

    public ReviewService(ServiceRequestService serviceRequestService,
                         NotificationService notificationService) {
        this.serviceRequestService = serviceRequestService;
        this.notificationService = notificationService;

    }

    /**
     * Business rules:
     * - Request must exist.
     * - Must belong to the same customer.
     * - Must have staff assigned.
     * - Must be COMPLETED.
     */
    public void submit(String requestId, String rating, String message, String customerId) {
        ServiceRequest req = serviceRequestService.getRequestById(requestId);

        // Own request?
        if (!req.getClientId().equalsIgnoreCase(customerId)) {
            throw new RuntimeException("You can only review your own requests.");
        }

        // Staff assigned?
        String staffId = req.getAssignedStaffId();
        if (staffId == null || "null".equalsIgnoreCase(staffId)) {
            throw new RuntimeException("You can review only after a staff has been assigned.");
        }

        // Completed?
        if (!"COMPLETED".equalsIgnoreCase(req.getStatus())) {
            throw new RuntimeException("You can review only COMPLETED requests.");
        }

        // Persist review using your existing file-based logic
        Review.submitReview(requestId, rating, message);

        // Notify admin
        notificationService.notifyAdmin(
                "Customer " + customerId + " submitted a review for request " +
                        requestId + " with rating " + rating + "."
        );

        // Notify staff who handled it
        notificationService.sendNotification(
                staffId,
                "New Review",
                "Customer " + customerId + " submitted a review for request " +
                        requestId + " with rating " + rating + "."
        );
    }

    public List<Review> getAll() {
        return Review.readAllReviewsFromArchive();
    }

    public boolean existsForRequest(String requestId) {
        return Review.isReviewSubmitted(requestId);
    }
}
