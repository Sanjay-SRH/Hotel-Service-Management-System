package com.intellisix.service_request_management.controller;

import com.intellisix.service_request_management.model.Role;
import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.service.UserService;
import com.intellisix.service_request_management.storage.Authentication;
import com.intellisix.service_request_management.dto.LoginRequestDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/**
 * REST Controller for handling user authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final UserService userService;

    public AuthRestController(UserService userService) {
        this.userService = userService;
    }

    // =========================================================
    // 1. BASIC AUTH LOGIN / VERIFICATION (The "Login" Check)
    // =========================================================
    // GET http://localhost:8080/api/auth/verify
    // To use: Go to Postman "Authorization" tab -> Select "Basic Auth" -> Enter Creds
    @GetMapping("/verify")
    public ResponseEntity<?> verifyBasicAuth(Principal principal) {
        // 'Principal' is injected by Spring Security only if the Basic Auth header is valid.
        if (principal == null) {
            return new ResponseEntity<>("Not authenticated", HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok("Login Successful! You are logged in as: " + principal.getName());
    }

    // =========================================================
    // 2. MANUAL JSON LOGIN (Optional / Legacy)
    // =========================================================
    // POST http://localhost:8080/api/auth/login
    // This remains public (permitAll) in SecurityConfig so users can login via JSON body if needed.
    @PostMapping("/login")
    public ResponseEntity<?> loginDisabled() {
        return new ResponseEntity<>(
                "This login endpoint is disabled. Please use Basic Auth in Postman.",
                HttpStatus.FORBIDDEN
        );
    }
    @GetMapping("/logout")
    public ResponseEntity<?> fakeLogout() {
        return ResponseEntity.ok("Logout successful (Basic Auth has no server-side sessions; please clear your credentials in the client).");
    }

}