package com.intellisix.service_request_management.service;

import java.util.List;
import java.util.stream.Collectors;

import com.intellisix.service_request_management.model.Notification;
import com.intellisix.service_request_management.storage.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void sendNotification(String recipientId, String message) {
        Notification n = new Notification(recipientId, message);
        repo.save(n);
    }

    public void sendNotification(String recipientId, String title, String message) {
        Notification n = new Notification(recipientId, title, message);
        repo.save(n);
    }

    public void notifyAdmin(String message) {
        sendNotification("A001", "Admin Alert", message);
    }

    public List<Notification> getNotificationsForUser(String recipientId) {
        List<Notification> notifications = repo.findByRecipientId(recipientId);
        notifications.sort((n1, n2) -> n2.getCreatedAt().compareTo(n1.getCreatedAt()));
        return notifications;
    }

    public List<Notification> getUnreadNotifications(String recipientId) {
        return getNotificationsForUser(recipientId).stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }

    public void markAllAsRead(String recipientId) {
        List<Notification> unread = getUnreadNotifications(recipientId);
        for (Notification n : unread) {
            n.markAsRead();
            repo.update(n);
        }
    }
}
