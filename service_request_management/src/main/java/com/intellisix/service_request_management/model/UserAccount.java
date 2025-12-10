package com.intellisix.service_request_management.model;

/**
 * Base class for all user types: Admin, Staff, Customer.
 *
 * Place at:
 * src/main/java/com/intellisix/service_request_management/model/UserAccount.java
 */
public class UserAccount {

    private String id;
    private String username;
    private String password;
    private Role role;
    private String name;

    public UserAccount() {}

    public UserAccount(String id, String username, String password, Role role, String name) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
    }

    public UserAccount(String id, String username, String password, Role role) {
        this(id, username, password, role, null);
    }

    // --------- Getters ---------
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    // --------- Setters ---------
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --------- Utility ---------

    @Override
    public String toString() {
        return "UserAccount [id=" + id +
                ", username=" + username +
                ", role=" + role + "]";
    }
}