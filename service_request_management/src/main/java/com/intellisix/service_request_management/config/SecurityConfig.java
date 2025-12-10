package com.intellisix.service_request_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for Postman testing
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/requests/all").hasRole("Admin")
                        .requestMatchers(HttpMethod.PUT, "/api/requests/*/assign").hasRole("Admin")
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("Admin")
                        .requestMatchers(HttpMethod.GET, "/api/reviews").hasRole("Admin")

                        .requestMatchers(HttpMethod.PUT, "/api/requests/*/status").hasRole("Staff")
                        .requestMatchers(HttpMethod.GET, "/api/requests/staff/**").hasAnyRole("Staff","Admin")

                        .requestMatchers(HttpMethod.POST, "/api/requests").hasRole("Customer")
                        .requestMatchers(HttpMethod.GET, "/api/requests/client/**").hasAnyRole("Customer","Admin")
                        .requestMatchers(HttpMethod.PUT, "/api/requests/*/cancel").hasRole("Customer")
                        .requestMatchers(HttpMethod.POST, "/api/reviews").hasRole("Customer")

                        .requestMatchers("/api/auth/verify").authenticated()
                        .requestMatchers("/api/notifications/**").authenticated()

                        // block JSON login confusion:
                        .requestMatchers("/api/auth/login").denyAll()

                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults()); // Enables the Pop-up / Postman "Basic Auth" tab

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Passwords are stored as plain text in your JSON, so we use NoOp encoder.
        return NoOpPasswordEncoder.getInstance();
    }
}