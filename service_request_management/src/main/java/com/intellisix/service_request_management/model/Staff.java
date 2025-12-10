package com.intellisix.service_request_management.model;

/**
 * Staff model extending UserAccount.
 *
 * Place at:
 * src/main/java/com/intellisix/service_request_management/model/Staff.java
 */
public class Staff extends UserAccount {

    public Staff(String id, String username, String password, String name) {
        super(id, username, password, Role.Staff, name);
    }
}
