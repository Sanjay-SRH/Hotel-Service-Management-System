package com.intellisix.service_request_management.controller;

import com.intellisix.service_request_management.model.Notification;
import com.intellisix.service_request_management.model.Role;
import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.service.NotificationService;
import com.intellisix.service_request_management.storage.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;

    public NotificationRestController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Helper: checks if logged-in user can access given userId
    private ResponseEntity<?> checkAccess(String userId, Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);
        }

        String username = principal.getName();
        UserAccount account = Authentication.findByUsername(username).orElse(null);
        if (account == null) {
            return new ResponseEntity<>("Logged-in user not found.", HttpStatus.UNAUTHORIZED);
        }

        // Admin can see everyone
        if (account.getRole() == Role.Admin) {
            return null; // allowed
        }

        // Other roles: only their own notifications
        if (!account.getId().equalsIgnoreCase(userId)) {
            return new ResponseEntity<>("Access denied: You can view only your own notifications.",
                    HttpStatus.FORBIDDEN);
        }

        return null; // allowed
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserNotifications(@PathVariable String userId, Principal principal) {
        ResponseEntity<?> access = checkAccess(userId, principal);
        if (access != null) return access;

        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{userId}/unread")
    public ResponseEntity<?> getUnreadNotifications(@PathVariable String userId, Principal principal) {
        ResponseEntity<?> access = checkAccess(userId, principal);
        if (access != null) return access;

        List<Notification> unread = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(unread);
    }

    @PutMapping("/{userId}/read")
    public ResponseEntity<?> markAllRead(@PathVariable String userId, Principal principal) {
        ResponseEntity<?> access = checkAccess(userId, principal);
        if (access != null) return access;

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read.");
    }
}
