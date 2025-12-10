package com.intellisix.service_request_management.service;


import java.util.Optional;

import com.intellisix.service_request_management.model.Client;
import com.intellisix.service_request_management.model.Role;
import com.intellisix.service_request_management.model.Staff;
import com.intellisix.service_request_management.model.Admin;

import com.intellisix.service_request_management.model.Client;
import com.intellisix.service_request_management.model.Staff;
import com.intellisix.service_request_management.model.Admin;   // <-- ADD THIS
import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.storage.Authentication;
import org.springframework.stereotype.Service;

import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.storage.Authentication;

import org.springframework.stereotype.Service;
/**
 * UserService handles all user operations:
 * - Staff registration
 * - Customer registration
 * - Authentication
 * - Room-number lookup for service request IDs
 * - Account updates
 *
 * Place in:
 * src/main/java/com/intellisix/service_request_management/service/UserService.java
 */
@Service
public class UserService {
    private final NotificationService  notificationService;
    private static final String ADMIN_NOTIFICATION_ID = "A001";

    public UserService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // -----------------------------
    //      REGISTER STAFF
    // -----------------------------
    public String registerStaff(String username, String password, String name) {

        if (Authentication.findByUsername(username).isPresent()) {
            System.out.println("*** Registration failed: username already taken. ***");
            return null;
        }

        String newId = Authentication.generateIdForRolePrefix("S");
        Staff staff = new Staff(newId, username, password, name);

        boolean ok = Authentication.persistNewAccount(staff);

        if (ok) {
            System.out.println("Registered new staff with id: " + newId);
            String message = "New Staff account created: " + name + " (ID: " + newId + "). Username: " + username;
            notificationService.sendNotification(ADMIN_NOTIFICATION_ID, message);
            return newId;
        } else {
            return null;
        }
    }

    // -----------------------------
    //     REGISTER CUSTOMER
    // -----------------------------
    public String registerCustomer(String name, String username, String password, String roomNumber) {

        if (Authentication.findByUsername(username).isPresent()) {
            System.out.println("*** Registration failed: username already taken. ***");
            return null;
        }

        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Room number is required for customer registration.");
        }

        if (Authentication.findClientByRoomNumber(roomNumber).isPresent()) {
            throw new  IllegalArgumentException("Room number '" + roomNumber + "' is already assigned to another customer.");
        }

        String newId = Authentication.generateIdForRolePrefix("C");

        Client client = new Client(newId, username, password, roomNumber, name);

        boolean ok = Authentication.persistNewAccount(client);

        if (ok) {
            System.out.println("Registered new customer with id: " + newId + "in room: " + roomNumber);
            String message = "New Client account created: " + name + " (ID: " + newId + ", Room: " + roomNumber + "). Username: " + username;
            notificationService.sendNotification(ADMIN_NOTIFICATION_ID, message);
            return newId;
        } else {
            return null;
        }
    }

    // -----------------------------
    //   GET ROOM NUMBER
    // -----------------------------
    /**
     * Finds the room number associated with a client ID.
     * Used by RequestIdUtil for generating smart Request IDs.
     */
    public static String getRoomNumberByClientId(String clientId) {

        Optional<UserAccount> accountOpt = Authentication.findById(clientId);

        if (accountOpt.isPresent() && accountOpt.get().getRole() == Role.Customer) {

            UserAccount acc = accountOpt.get();

            // Client is a subclass of UserAccount
            if (acc instanceof Client) {
                return ((Client) acc).getRoomNumber();
            }
        }

        return "UNKNWN"; // fallback
    }

    // -----------------------------
    //        AUTHENTICATION
    // -----------------------------
    public static Role authenticate(String username, String password, Role expectedRole) {

        return Authentication.findByUsername(username)
                .filter(acc -> acc.getPassword().equals(password)
                        && acc.getRole() == expectedRole)
                .map(UserAccount::getRole)
                .orElse(null);
    }

    // -----------------------------
    //        UPDATE USER (NEW)
    // -----------------------------
    /**
     * Updates an existing UserAccount (Staff, Admin, or Customer)
     * by ID and persists the changes.
     */

    public String registerAdmin(Admin admin) {
        // Generate ID for admin like A001, A002...
        String newId = Authentication.generateIdForRolePrefix("A");
        admin.setId(newId);

        boolean success = Authentication.persistNewAccount(admin);
        return success ? newId : null;
    }


    public static UserAccount updateAccount(UserAccount update) {
        Optional<UserAccount> existingOpt = Authentication.findById(update.getId());

        if (existingOpt.isEmpty()) {
            System.err.println("[ERROR] Account not found for update: " + update.getId());
            return null;
        }

        UserAccount existing = existingOpt.get();

        // NEW: Update Name if provided
        if (update.getName() != null && !update.getName().isEmpty()) {
            existing.setName(update.getName());
        }

        // 1. Update Username if provided and different
        if (update.getUsername() != null && !update.getUsername().isEmpty() && !update.getUsername().equalsIgnoreCase(existing.getUsername())) {

            // Check if the new username is already taken by another account (excluding itself)
            Optional<UserAccount> duplicateUser = Authentication.findByUsername(update.getUsername());
            if (duplicateUser.isPresent() && !duplicateUser.get().getId().equalsIgnoreCase(existing.getId())) {
                System.out.println("*** Update failed: username '" + update.getUsername() + "' is already taken. ***");
                return null;
            }
            existing.setUsername(update.getUsername());
        }

        // 2. Update Password if provided
        if (update.getPassword() != null && !update.getPassword().isEmpty()) {
            existing.setPassword(update.getPassword());
        }

        // 3. Update Role if provided and different
        if (update.getRole() != null && update.getRole() != existing.getRole()) {
            existing.setRole(update.getRole());
        }

        // 4. Handle Client-specific update (Room Number)
        if (existing instanceof Client && update instanceof Client) {
            Client existingClient = (Client) existing;
            Client updateClient = (Client) update;
            // The Client class now has a setter for room number (from previous updates)
            if (updateClient.getRoomNumber() != null && !updateClient.getRoomNumber().isEmpty()) {
                existingClient.setRoomNumber(updateClient.getRoomNumber());
            }
        }

        // Persist the changes
        Authentication.updateAccount(existing);
        System.out.println("[SUCCESS] Account updated for user ID: " + existing.getId());
        return existing;
    }
}