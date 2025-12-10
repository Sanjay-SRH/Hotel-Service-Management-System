package com.intellisix.service_request_management.service;

import java.util.List;
// import java.util.Random; // Removed: Not needed if using RequestIdUtil
import java.time.LocalDateTime;

import com.intellisix.service_request_management.model.ServiceRequest;
import com.intellisix.service_request_management.storage.ServiceRequestRepository;
import com.intellisix.service_request_management.storage.Authentication;
import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.model.Role;
import org.springframework.stereotype.Service;

@Service
public class ServiceRequestService {

    private final ServiceRequestRepository repo;
    // private final Random random = new Random(); // Removed: Not needed if using RequestIdUtil

    private static final int MAX_ACTIVE_REQUESTS = 5;
    private final NotificationService notificationService;

    public ServiceRequestService(ServiceRequestRepository repo, NotificationService notificationService) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    // -------------------------------
    //        CREATE REQUEST
    // -------------------------------
    public ServiceRequest createServiceRequest(String clientId, String serviceType, String description, String priority) {
        // FIX: Replaced custom generateSmartId with utility class method
        String requestId = RequestIdUtil.generateId(serviceType, clientId);
        ServiceRequest r = new ServiceRequest(requestId, clientId, serviceType, description, priority);
        r.createRequest();
        repo.save(r);

        // Notify customer
        notificationService.sendNotification(
                clientId,
                "Request Created",
                "Your new request (" + requestId + ") for " + serviceType +
                        " has been successfully created. Status: PENDING."
        );

        // Notify admin
        notificationService.notifyAdmin(
                "New service request created: " + requestId +
                        " by customer " + clientId +
                        " for service type " + serviceType + "."
        );

        return r;
    }

    // REMOVED: private String generateSmartId(String serviceType, String clientId) { ... }

    // -------------------------------
    //        READ REQUESTS
    // -------------------------------
    public ServiceRequest getRequestById(String requestId) {
        ServiceRequest r = repo.findById(requestId);
        if (r == null)
            throw new RuntimeException("Request not found: " + requestId);
        return r;
    }

    public List<ServiceRequest> getAllRequests() {
        return repo.findAll();
    }

    public List<ServiceRequest> getRequestsByClient(String clientId) {
        return repo.findByClientId(clientId);
    }

    public List<ServiceRequest> getRequestsByStaff(String staffId) {
        return repo.findByStaffId(staffId);
    }

    // -------------------------------
    //       UPDATE REQUEST STATUS
    // -------------------------------
    public ServiceRequest updateRequestStatus(String requestId, String newStatus, String notes, String actorId) {
        ServiceRequest r = repo.findById(requestId);
        if (r == null) {
            // FIX: Corrected typo "withID"
            throw new RuntimeException("Request not found with ID: " + requestId);
        }

        // >>> START OF FIX: Prevent staff from updating a CANCELLED request <<<
        if ("CANCELLED".equalsIgnoreCase(r.getStatus())) {
            throw new RuntimeException("Request ID " + requestId + " is already CANCELLED and cannot be updated.");
        }
        // >>> END OF FIX <<<

        // FIX: Define oldStatus before any updates
        String oldStatus = r.getStatus();

        // --- AUTHORIZATION CHECK (Staff Ownership) ---
        UserAccount actorAccount = Authentication.findById(actorId)
                .orElseThrow(() -> new RuntimeException("Actor user account not found for ID: " + actorId));

        // Only staff must be checked for ownership, Admins can update anything.
        if (actorAccount.getRole() == Role.Staff) {
            String assignedStaffId = r.getAssignedStaffId();

            // Check if the request is assigned to a specific staff member
            if (assignedStaffId != null && !"null".equalsIgnoreCase(assignedStaffId)) {
                // If assigned, the actorId MUST match the assignedStaffId
                if (!assignedStaffId.equalsIgnoreCase(actorId)) {
                    throw new RuntimeException("Authorization Error: You can only update requests that are assigned to you.");
                }
            }
        }
        // --- END AUTHORIZATION CHECK ---

        // --- STATUS TRANSITION & ASSIGNMENT LOGIC (FIXED) ---

        if ("COMPLETED".equalsIgnoreCase(newStatus) || "CLOSED".equalsIgnoreCase(newStatus)) {
            // If the request was not assigned, it will now be assigned to the one completing it
            if (r.getAssignedStaffId() == null || "null".equalsIgnoreCase(r.getAssignedStaffId())) {
                r.assignStaff(actorId);
            }
            // Now correctly calls the 3-argument method in ServiceRequest
            r.completeRequest(newStatus, notes, actorId);

        } else if ("IN_PROGRESS".equalsIgnoreCase(newStatus)) {
            // Staff member is claiming an unassigned request
            if (r.getAssignedStaffId() == null || "null".equalsIgnoreCase(r.getAssignedStaffId())) {
                r.assignStaff(actorId); // Automatically assigns the actor
            }
            // Now correctly calls the 2-argument method in ServiceRequest
            r.updateStatus(newStatus, notes);

        } else {
            // Simple status update for other transitions (e.g., PENDING -> ASSIGNED by Admin)
            // Now correctly calls the 2-argument method in ServiceRequest
            r.updateStatus(newStatus, notes);
        }

        repo.update(r);

        // Notify customer
        String baseMsg = "Your request (" + requestId + ") status changed from " +
                oldStatus + " to " + newStatus + "."; // <-- FIX: oldStatus is now defined
        if (notes != null && !notes.isBlank()) {
            baseMsg += " Notes: " + notes;
        }
        notificationService.sendNotification(
                r.getClientId(),
                "Request Status Update",
                baseMsg
        );

        // Notify admin
        String adminMsg = "Request " + requestId + " status changed from " +
                oldStatus + " to " + newStatus + " by staff " + actorId + ".";
        notificationService.notifyAdmin(adminMsg);

        // Notify staff assigned (if any)
        // Use the potentially new assigned staff ID after update
        String assignedStaffId = r.getAssignedStaffId();
        if (assignedStaffId != null && !"null".equalsIgnoreCase(assignedStaffId)) {
            notificationService.sendNotification(
                    assignedStaffId,
                    "Request Status Update",
                    "Status of request (" + requestId + ") for customer " +
                            r.getClientId() + " changed from " + oldStatus + " to " + newStatus + "."
            );
        }

        return r;
    }

    // -------------------------------
    //        CANCEL REQUEST
    // -------------------------------
    public ServiceRequest cancelRequest(String requestId, String username) {
        ServiceRequest r = repo.findById(requestId);
        if (r == null) {
            throw new RuntimeException("Request not found with ID: " + requestId);
        }

        UserAccount loggedInUser = Authentication.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged-in user not found."));

        // *** NEW VALIDATION LOGIC START ***
        // r.isTerminal() is now resolved in ServiceRequest.java
        if (r.isTerminal()) {
            throw new RuntimeException("Request " + requestId + " is already " + r.getStatus() + " and cannot be cancelled.");
        }
        // *** NEW VALIDATION LOGIC END ***

        r.setStatus("CANCELLED");
        r.setUpdatedAt(LocalDateTime.now());
        // No need to set completedAt or completionNotes for cancellation

        repo.update(r); // Persist the change

        // Notify staff if one was assigned
        if (r.getAssignedStaffId() != null && !"null".equalsIgnoreCase(r.getAssignedStaffId())) {
            notificationService.sendNotification(
                    r.getAssignedStaffId(),
                    "Request Cancelled",
                    "Service request " + requestId + " has been cancelled by the customer."
            );
        }

        // Notify admin
        notificationService.notifyAdmin(
                "Request " + requestId + " was cancelled by customer " + r.getClientId() + "."
        );

        return r;
    }

    // -------------------------------
    //       ASSIGN STAFF
    // -------------------------------
    public ServiceRequest assignStaffToRequest(String requestId, String staffId) {

        ServiceRequest r = repo.findById(requestId);
        if (r == null)
            throw new RuntimeException("Request not found: " + requestId);

        if ("COMPLETED".equalsIgnoreCase(r.getStatus()) ||
                "CANCELLED".equalsIgnoreCase(r.getStatus())) {

            throw new RuntimeException(
                    "Action Blocked: Cannot assign staff to a " + r.getStatus() + " request."
            );
        }

        // FIX: Replaced non-existent Authentication.staffExists with a check for existence and role.
        UserAccount staffAccount = Authentication.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff ID " + staffId + " not found in credentials."));
        if (staffAccount.getRole() != Role.Staff) {
            throw new RuntimeException("User ID " + staffId + " is not a Staff member.");
        }

        long activeCount = countActiveRequestsForStaff(staffId);

        if (activeCount >= MAX_ACTIVE_REQUESTS)
            throw new RuntimeException(
                    "Staff ID " + staffId + " is unavailable. They already have " +
                            activeCount + " active requests (max: " + MAX_ACTIVE_REQUESTS + ")."
            );

        r.assignStaff(staffId);
        repo.update(r);

        // Notify staff
        notificationService.sendNotification(
                staffId,
                "New Assignment",
                "You have been assigned to a new service request: " + requestId +
                        ". Type: " + r.getServiceType() + "."
        );

        // Notify customer
        notificationService.sendNotification(
                r.getClientId(),
                "Staff Assigned",
                "Your request (" + requestId + ") has been assigned to staff " +
                        staffId + ". Status is now ASSIGNED."
        );

        // Notify admin
        notificationService.notifyAdmin(
                "Request " + requestId + " has been assigned to staff " + staffId +
                        " for customer " + r.getClientId() + "."
        );

        return r;
    }

    // -------------------------------
    //        STAFF WORKLOAD
    // -------------------------------
    public long countActiveRequestsForStaff(String staffId) {
        return repo.findByStaffId(staffId).stream()
                .filter(r -> "ASSIGNED".equalsIgnoreCase(r.getStatus()) ||
                        "IN_PROGRESS".equalsIgnoreCase(r.getStatus()))
                .count();
    }
}