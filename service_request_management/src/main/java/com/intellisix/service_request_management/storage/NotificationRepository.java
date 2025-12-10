package com.intellisix.service_request_management.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.intellisix.service_request_management.model.Notification;
import org.springframework.stereotype.Repository;
/**
 * Repository for managing Notification objects in memory + JSON persistence.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/stroage/NotificationRepository.java
 */
@Repository
public class NotificationRepository {

    private List<Notification> store;
    private final NotificationFilePersistence filePersistence;

    public NotificationRepository(NotificationFilePersistence filePersistence) {
        this.filePersistence = filePersistence;

        this.store = filePersistence.loadAll();
        if (this.store == null) {
            this.store = new ArrayList<>();
        }
    }

    /**
     * Saves a new Notification object.
     * Generates a new ID and timestamp if needed.
     */
    public void save(Notification notification) {

        if (notification.getId() == null) {
            String newId = UUID.randomUUID().toString();

            Notification newNotification = new Notification(
                    newId,
                    notification.getRecipientId(),
                    notification.getTitle(),
                    notification.getMessage(),
                    LocalDateTime.now(),
                    notification.isRead()
            );

            store.add(newNotification);
        } else {
            store.add(notification);
        }

        filePersistence.saveAll(store);
    }

    public List<Notification> findByRecipientId(String recipientId) {
        return store.stream()
                .filter(n -> n.getRecipientId().equalsIgnoreCase(recipientId))
                .collect(Collectors.toList());
    }

    public Notification findById(String id) {
        return store.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Updates an existing notification in the store.
     */
    public void update(Notification updatedNotification) {
        boolean removed = store.removeIf(n -> n.getId().equals(updatedNotification.getId()));
        if (removed) {
            store.add(updatedNotification);
            filePersistence.saveAll(store);
        } else {
            System.err.println("Notification update failed: ID not found in store: " + updatedNotification.getId());
        }
    }
}
