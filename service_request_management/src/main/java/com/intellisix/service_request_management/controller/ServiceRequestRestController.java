package com.intellisix.service_request_management.controller;

import com.intellisix.service_request_management.dto.CreateRequestDTO;
import com.intellisix.service_request_management.model.Role;
import com.intellisix.service_request_management.model.ServiceRequest;
import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.service.ServiceRequestService;
import com.intellisix.service_request_management.storage.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * REPLACES: CustomerServiceRequestController.java
 * This REST Controller handles all interactions for Service Requests via Postman/Web.
 */
@RestController
@RequestMapping("/api/requests")
public class ServiceRequestRestController {

    private final ServiceRequestService serviceRequestService;

    public ServiceRequestRestController(ServiceRequestService serviceRequestService) {
        this.serviceRequestService = serviceRequestService;
    }

    // ==================================================================================
    // 1. CREATE REQUEST (Customer creates request – clientId taken from logged-in user)
    // ==================================================================================
    // Endpoint: POST http://localhost:8080/api/requests
    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody CreateRequestDTO requestDto, Principal principal) {
        try {
            // Must be authenticated
            if (principal == null) {
                return new ResponseEntity<>("Not authenticated.", HttpStatus.UNAUTHORIZED);
            }

            // Find logged-in user
            String username = principal.getName();
            UserAccount account = Authentication.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Logged-in user not found in storage."));

            // Only customers can create requests (defensive check)
            if (account.getRole() != Role.Customer) {
                return new ResponseEntity<>("Only customers can create service requests.", HttpStatus.FORBIDDEN);
            }

            // Validate description (we now ignore clientId from body)
            if (requestDto.getDescription() == null || requestDto.getDescription().trim().isEmpty()) {
                return new ResponseEntity<>("Error: Description is required.", HttpStatus.BAD_REQUEST);
            }

            // Take clientId from the logged-in user's account
            String clientId = account.getId();

            ServiceRequest newRequest = serviceRequestService.createServiceRequest(
                    clientId,
                    requestDto.getServiceType(),
                    requestDto.getDescription(),
                    requestDto.getPriority()
            );

            return new ResponseEntity<>(newRequest, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================================================================================
    // 2. GET ALL REQUESTS (Admin View)
    // ==================================================================================
    // Endpoint: GET http://localhost:8080/api/requests/all
    @GetMapping("/all")
    public ResponseEntity<List<ServiceRequest>> getAllRequests() {
        return ResponseEntity.ok(serviceRequestService.getAllRequests());
    }

    // ==================================================================================
    // 3. GET CLIENT REQUESTS (Customer View)
    // ==================================================================================
    // Endpoint: GET http://localhost:8080/api/requests/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getClientRequests(@PathVariable String clientId, Principal principal) {

        // logged-in username
        String loggedInUser = principal.getName();
        UserAccount account = Authentication.findByUsername(loggedInUser).orElse(null);

        if (account == null || !account.getId().equals(clientId)) {
            return new ResponseEntity<>("Access denied: You can view ONLY your own requests.", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(serviceRequestService.getRequestsByClient(clientId));
    }


    // ==================================================================================
    // 4. GET STAFF REQUESTS (Staff View)
    // ==================================================================================
    // Endpoint: GET http://localhost:8080/api/requests/staff/{staffId}
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getStaffRequests(@PathVariable String staffId, Principal principal) {

        String loggedInUser = principal.getName();
        UserAccount account = Authentication.findByUsername(loggedInUser).orElse(null);

        if (account == null || !account.getId().equals(staffId)) {
            return new ResponseEntity<>("Access denied: You can view ONLY your own assigned requests.", HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(serviceRequestService.getRequestsByStaff(staffId));
    }


    // ==================================================================================
    // 5. ASSIGN STAFF (Admin Action)
    // ==================================================================================
    // Endpoint: PUT http://localhost:8080/api/requests/{requestId}/assign?staffId=S001
    @PutMapping("/{requestId}/assign")
    public ResponseEntity<?> assignStaff(@PathVariable String requestId, @RequestParam String staffId) {
        // Validation check for empty parameter
        if (staffId == null || staffId.trim().isEmpty()) {
            return new ResponseEntity<>("Error: staffId parameter cannot be empty.", HttpStatus.BAD_REQUEST);
        }

        try {
            ServiceRequest req = serviceRequestService.assignStaffToRequest(requestId, staffId);
            return ResponseEntity.ok(req);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ==================================================================================
    // 6. UPDATE STATUS (Staff Action)
    // ==================================================================================
    // Endpoint: PUT http://localhost:8080/api/requests/{requestId}/status?status=COMPLETED&actorId=S001&notes=Done
    @PutMapping("/{requestId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable String requestId,
            @RequestParam String status,
            @RequestParam(required = false) String notes,
            Principal principal){
        if (principal == null) {
            return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);
        }
        try {
            String username = principal.getName();
            UserAccount account =Authentication.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Logged-in user not found."));
            String actorId = account.getId();

            ServiceRequest req = serviceRequestService.updateRequestStatus(requestId, status, notes, actorId);
            return ResponseEntity.ok(req);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ==================================================================================
    // 7. CANCEL REQUEST (Customer Action)
    // ==================================================================================
    // Endpoint: PUT http://localhost:8080/api/requests/{requestId}/cancel
    @PutMapping("/{requestId}/cancel")
    public ResponseEntity<?> cancelRequest(@PathVariable String requestId, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("Not authenticated.", HttpStatus.UNAUTHORIZED);
        }
        try {
            // PASS THE USERNAME (principal.getName()) to the service layer
            ServiceRequest req = serviceRequestService.cancelRequest(requestId, principal.getName());
            return ResponseEntity.ok(req);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}
