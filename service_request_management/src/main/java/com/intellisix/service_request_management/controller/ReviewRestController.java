package com.intellisix.service_request_management.controller;

import com.intellisix.service_request_management.dto.ReviewDTO;
import com.intellisix.service_request_management.model.Review;
import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.service.ReviewService;
import com.intellisix.service_request_management.storage.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewRestController {

    private final ReviewService reviewService;

    public ReviewRestController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody ReviewDTO reviewDto, Principal principal) {
        try {
            if (principal == null) {
                return new ResponseEntity<>("Not authenticated.", HttpStatus.UNAUTHORIZED);
            }

            String username = principal.getName();
            UserAccount account = Authentication.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

            String customerId = account.getId();

            // Prevent duplicate review for the same request (optional)
            if (reviewService.existsForRequest(reviewDto.getRequestId())) {
                return new ResponseEntity<>("Review already exists for this request.", HttpStatus.CONFLICT);
            }

            reviewService.submit(
                    reviewDto.getRequestId(),
                    reviewDto.getRating(),
                    reviewDto.getReviewMessage(),
                    customerId
            );

            return new ResponseEntity<>("Review submitted successfully.", HttpStatus.CREATED);

        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAll());
    }
}
