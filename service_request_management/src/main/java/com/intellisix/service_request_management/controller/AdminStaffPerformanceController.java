package com.intellisix.service_request_management.controller;

import com.intellisix.service_request_management.dto.StaffPerformanceDTO;
import com.intellisix.service_request_management.model.Role;
import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.service.ReviewService;
import com.intellisix.service_request_management.storage.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin")
public class AdminStaffPerformanceController {

    private final ReviewService reviewService;

    public AdminStaffPerformanceController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/staff/{staffId}/performance")
    public ResponseEntity<?> getStaffPerformance(@PathVariable String staffId,
                                                 Principal principal) {

        if (principal == null) {
            return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String username = principal.getName();
        UserAccount account = Authentication.findByUsername(username).orElse(null);

        if (account == null || account.getRole() != Role.Admin) {
            return new ResponseEntity<>("Access denied: Admin only.", HttpStatus.FORBIDDEN);
        }

        StaffPerformanceDTO dto = reviewService.getStaffPerformance(staffId);
        return ResponseEntity.ok(dto);
    }
}
