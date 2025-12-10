package com.intellisix.service_request_management.model;

/**
 * Admin user model — extends the base UserAccount with the Admin role.
 * Place this file in:
 * src/main/java/com/intellisix/service_request_management/model/Admin.java
 */
public class Admin extends UserAccount {
    public Admin(String id, String username, String password) {
        super(id, username, password, Role.Admin);
    }
}
