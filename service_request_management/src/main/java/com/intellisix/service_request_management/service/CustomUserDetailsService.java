package com.intellisix.service_request_management.service;

import com.intellisix.service_request_management.model.UserAccount;
import com.intellisix.service_request_management.storage.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Bridges JSON storage with Spring Security.
 * Loads user data and assigns permissions based on Roles.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Look up user in your existing JSON storage
        Optional<UserAccount> accountOpt = Authentication.findByUsername(username);

        if (accountOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        UserAccount account = accountOpt.get();

        // 2. Convert to Spring Security User
        // Spring Security expects roles to be passed here.
        // We use the account.getRole().name() which returns "Admin", "Staff", or "Customer".
        return User.builder()
                .username(account.getUsername())
                .password(account.getPassword())
                .roles(account.getRole().name()) // This sets authority as "ROLE_Admin", "ROLE_Staff", etc.
                .build();
    }
}