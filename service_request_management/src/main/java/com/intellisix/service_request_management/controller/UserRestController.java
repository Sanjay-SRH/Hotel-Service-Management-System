package com.intellisix.service_request_management.controller;

import com.intellisix.service_request_management.dto.RegisterCustomerDTO;
import com.intellisix.service_request_management.dto.RegisterStaffDTO;
import com.intellisix.service_request_management.model.Admin;
import com.intellisix.service_request_management.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    // ==================================================================================
    // 1. REGISTER CUSTOMER (ADMIN ONLY)
    // ==================================================================================
    // POST http://localhost:8080/api/users/customer
    @PostMapping("/customer")
    public ResponseEntity<?> registerCustomer(@RequestBody RegisterCustomerDTO dto) {
        if (dto.getUsername() == null || dto.getPassword() == null || dto.getRoomNumber() == null) {
            return new ResponseEntity<>("Username, Password and Room Number are required", HttpStatus.BAD_REQUEST);
        }

        try {
            String newId = userService.registerCustomer(
                    dto.getName(),
                    dto.getUsername(),
                    dto.getPassword(),
                    dto.getRoomNumber()
            );

            if (newId != null) {
                return new ResponseEntity<>("Customer registered successfully. ID: " + newId, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("Registration failed. Username is already be taken.", HttpStatus.CONFLICT);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>("Registration failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ==================================================================================
    // 2. REGISTER STAFF (ADMIN ONLY)
    // ==================================================================================
    // POST http://localhost:8080/api/users/staff
    @PostMapping("/staff")
    public ResponseEntity<?> registerStaff(@RequestBody RegisterStaffDTO dto) {
        if (dto.getUsername() == null || dto.getPassword() == null) {
            return new ResponseEntity<>("Username and Password are required", HttpStatus.BAD_REQUEST);
        }

        String newId = userService.registerStaff(
                dto.getUsername(),
                dto.getPassword(),
                dto.getName()
        );

        if (newId != null) {
            return new ResponseEntity<>("Staff registered successfully. ID: " + newId, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Registration failed. Username might be taken.", HttpStatus.CONFLICT);
        }
    }

    // ==================================================================================
    // 3. REGISTER ADMIN (optional)
    // ==================================================================================
    // POST http://localhost:8080/api/users/admin
    @PostMapping("/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Admin admin) {
        if (admin.getUsername() == null || admin.getPassword() == null) {
            return new ResponseEntity<>("Username and Password are required", HttpStatus.BAD_REQUEST);
        }

        String newId = userService.registerAdmin(admin);

        if (newId != null) {
            return new ResponseEntity<>("Admin registered successfully. ID: " + newId, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>("Registration failed. Username might be taken.", HttpStatus.CONFLICT);
        }
    }
}
