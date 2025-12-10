package com.intellisix.service_request_management.dto;

public class RegisterCustomerDTO {
    private String name;
    private String username;
    private String password;
    private String roomNumber;

    public RegisterCustomerDTO() {}

    public RegisterCustomerDTO(String name, String username, String password, String roomNumber) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.roomNumber = roomNumber;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}