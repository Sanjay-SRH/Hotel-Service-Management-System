package com.intellisix.service_request_management.dto;

/**
 * Data Transfer Object (DTO) for handling the login request body.
 * It maps the JSON from the frontend (username, password, role)
 * to a Java object for use in the AuthRestController.
 */
public class LoginRequestDTO {

    private String username;
    private String password;
    private String role;

    // Must have a default constructor for Spring/Jackson to create the object
    // when receiving a JSON payload.
    public LoginRequestDTO() {
    }

    // Constructor for convenience (optional but helpful)
    public LoginRequestDTO(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // --- Getters (Required by Spring Boot to read the field values) ---

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    // --- Setters (Required by Spring Boot to inject values from the JSON) ---

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}