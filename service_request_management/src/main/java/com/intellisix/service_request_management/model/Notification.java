package com.intellisix.service_request_management.model;

import java.time.LocalDateTime;

/**
 * Notification model used for user alerts.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/model/Notification.java
 */
public class Notification {

    // REMOVED 'final' keyword to allow setters/mutability for updates and loading
    private String id;
    private String recipientId;
    private String title; // NEW FIELD: Needed for setTitle() method in NotificationService
    private String message;
    private LocalDateTime createdAt;
    private boolean isRead;

    // NEW: No-args constructor (Fixes error at NotificationService.java:75)
    public Notification() {}

    // Full constructor (used when loading notifications from JSON file)
    public Notification(String id, String recipientId, String title, String message, LocalDateTime createdAt, boolean isRead) {
        this.id = id;
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Constructor for new notifications created at runtime (UPDATED to include title)
    public Notification(String recipientId, String title, String message) {
        this(null, recipientId, title, message, LocalDateTime.now(), false);
    }

    // Constructor for new notifications created at runtime (Kept original to support old service calls)
    public Notification(String recipientId, String message) {
        this(null, recipientId, "Alert", message, LocalDateTime.now(), false); // Added default title "Alert"
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getRecipientId() { return recipientId; }
    public String getTitle() { return title; } // NEW Getter
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public boolean isRead() {
        return isRead;
    }

    // --- Setters ---
    public void setId(String id) { this.id = id; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // setRead() handles the status update
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    // Mark as read utility
    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public String toString() {
        return "Notification [id=" + id +
                ", recipientId=" + recipientId +
                ", title=" + title +
                ", message=" + message +
                ", createdAt=" + createdAt +
                ", isRead=" + isRead + "]";
    }
}