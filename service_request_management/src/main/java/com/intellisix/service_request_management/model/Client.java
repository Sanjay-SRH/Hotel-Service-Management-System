package com.intellisix.service_request_management.model;

/**
 * Client user model — extends the base UserAccount and includes a room number.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/model/Client.java
 */
public class Client extends UserAccount {
    private String roomNumber; // NEW FIELD

    // UPDATED CONSTRUCTOR
    public Client(String id, String username, String password, String roomNumber, String name) {
        // Calls UserAccount constructor with the default Role.Customer
        super(id, username, password, Role.Customer, name);
        this.roomNumber = roomNumber;
    }

    // NEW GETTER
    public String getRoomNumber() {
        return roomNumber;
    }

    // Optionally add a setter if you need to modify the room number later:
    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }
}