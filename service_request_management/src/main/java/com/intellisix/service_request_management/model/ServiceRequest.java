package com.intellisix.service_request_management.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a service request submitted by a customer.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/model/ServiceRequest.java
 */
public class ServiceRequest {

    private String requestId;
    private String clientId;
    private String serviceType;
    private String description;
    private String priority;
    private String status;
    private String assignedStaffId;
    private String completionNotes;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // NEW: No-args constructor (Fixes error at ServiceRequestService.java:199)
    public ServiceRequest() {}

    // NEW: Full-parameter constructor for loading from JSON persistence
    // This constructor is required by ServiceRequestFilePersistence.java to load existing data.
    public ServiceRequest(String requestId, String clientId, String serviceType,
                          String description, String priority, String status,
                          String assignedStaffId, String completionNotes,
                          LocalDateTime createdAt, LocalDateTime updatedAt,
                          LocalDateTime completedAt) {
        this.requestId = requestId;
        this.clientId = clientId;
        this.serviceType = serviceType;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.assignedStaffId = assignedStaffId;
        this.completionNotes = completionNotes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
    }

    // Constructor used for creating a new service request
    public ServiceRequest(String requestId, String clientId, String serviceType,
                          String description, String priority) {
        this.requestId = requestId;
        this.clientId = clientId;
        this.serviceType = serviceType;
        this.description = description;
        this.priority = priority;
        this.status = "PENDING";
        this.assignedStaffId = "null";
        this.completionNotes = "null";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.completedAt = null;
    }

    // --- Getters ---

    public String getRequestId() {
        return requestId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public String getStatus() {
        return status;
    }

    public String getAssignedStaffId() {
        return assignedStaffId;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    // --- Setters / Update Methods ---

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if ("COMPLETED".equalsIgnoreCase(status) || "CANCELLED".equalsIgnoreCase(status)) {
            this.completedAt = LocalDateTime.now();
        } else {
            this.completedAt = null;
        }
    }

    public void setAssignedStaffId(String assignedStaffId) {
        this.assignedStaffId = assignedStaffId;
    }

    public void setCompletionNotes(String completionNotes) {
        this.completionNotes = completionNotes;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    // --- Utility Methods ---

    public void createRequest() {
        this.status = "PENDING";
        this.assignedStaffId = "null";
        this.completionNotes = "null";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.completedAt = null;
    }

    public void assignStaff(String staffId) {
        this.status = "ASSIGNED";
        this.assignedStaffId = staffId;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(String newStatus, String notes) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
        if (notes != null && !notes.trim().isEmpty()) {
            this.completionNotes = notes;
        }
        if ("COMPLETED".equalsIgnoreCase(newStatus) || "CANCELLED".equalsIgnoreCase(newStatus)) {
            this.completedAt = LocalDateTime.now();
        } else {
            this.completedAt = null;
        }
    }

    /**
     * FIX 1: Implements the missing method for ServiceRequestService.java:[119,14].
     * Marks the request as completed/updated by an actor (staff/admin).
     * @param newStatus The new status (e.g., "COMPLETED").
     * @param notes The completion notes.
     * @param actorId The ID of the staff/admin who completed it.
     */
    public void completeRequest(String newStatus, String notes, String actorId) {
        this.status = newStatus;
        this.completionNotes = notes;
        this.assignedStaffId = actorId;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * FIX 2: Implements the missing method for ServiceRequestService.java:[183,14].
     * Checks if the request is in a final, unchangeable state.
     * @return true if status is COMPLETED or CANCELLED.
     */
    public boolean isTerminal() {
        return "COMPLETED".equalsIgnoreCase(this.status) ||
                "CANCELLED".equalsIgnoreCase(this.status);
    }

    // For display only (to match the old format if desired)
    public String getFormattedCreatedAt() {
        return createdAt.format(DISPLAY_FMT);
    }

    public String getFormattedUpdatedAt() {
        return updatedAt.format(DISPLAY_FMT);
    }

    public String getFormattedCompletedAt() {
        return completedAt == null ? "N/A" : completedAt.format(DISPLAY_FMT);
    }

    @Override
    public String toString() {
        // ... (existing toString implementation)
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================");
        sb.append("\n Request ID   : ").append(requestId);
        sb.append("\n Client ID    : ").append(clientId);
        sb.append("\n Service Type : ").append(serviceType);
        sb.append("\n Description  : ").append(description);
        sb.append("\n Priority     : ").append(priority);
        sb.append("\n Status       : ").append(status);

        if (assignedStaffId != null && !assignedStaffId.equals("null")) {
            sb.append("\n Assigned To  : ").append(assignedStaffId).append("\n");
        }

        if (completionNotes != null && !completionNotes.equals("null")) {
            sb.append("\n Completion Notes: ").append(completionNotes).append("\n");
        }

        sb.append("\n ------------------------------------\n");
        sb.append("\n Created At   : ").append(getFormattedCreatedAt()).append("\n");
        sb.append("\n Updated At   : ").append(getFormattedUpdatedAt()).append("\n");
        if (completedAt != null) {
            sb.append("\n Completed At : ").append(getFormattedCompletedAt()).append("\n");
        }
        sb.append("\n========================================");

        return sb.toString();
    }
}