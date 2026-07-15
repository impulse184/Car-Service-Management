package com.carservice.gateway.controller;

import com.carservice.gateway.filter.JwtUtil;
import com.carservice.gateway.util.HashUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

// Manages the login, contacts user profile services, checks passwords and issues JWT tokens.
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate; 

    // Constructor for required dependencies
    public AuthController(JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    // Authenticates username and password and handles the login function
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> request) {
        String inputUsername = (String) request.get("username");
        String password = (String) request.get("password");

        // Stop null or empty inputs
        if (inputUsername == null || inputUsername.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password fields are mandatory.");
        }

        try {
            // Fetch all users from the service via RestTemplate
            String serviceUrl = "http://user-profile-service/users/userprofile";
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> allUsers = restTemplate.getForObject(serviceUrl, List.class);

            // Handle where user profile service returns an empty list
            if (allUsers == null || allUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }
            
            // Search to extract the user profile
            Map<String, Object> userProfile = allUsers.stream()
                    .filter(user -> inputUsername.trim().equalsIgnoreCase((String) user.get("username")))
                    .findFirst()
                    .orElse(null);

            // Reject if the username does not exist
            if (userProfile == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }
            
            // Extract password and role from user
            String storedHashedPassword = (String) userProfile.get("password");
            String realDatabaseRole = (String) userProfile.get("role");

            // If profile has no password, treat it as unauthenticated customer profile
            if (storedHashedPassword == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access Denied: Customer accounts do not require tokens.");
            }

            // Hash the password and verify the hash from user service
            String incomingPasswordHash = HashUtil.hashPassword(password);
            if (!incomingPasswordHash.equals(storedHashedPassword)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
            }

            // Extract the database user ID
            Long userId = ((Number) userProfile.get("id")).longValue();

            // Generate a signed JWT containing the user ID, username, and role
            String token = jwtUtil.generateToken(userId, inputUsername, realDatabaseRole);

            // Return user ID, username, role and token
            return ResponseEntity.ok(Map.of(
                "id", userId,
                "username", inputUsername,
                "role", realDatabaseRole,
                "token", token
            ));

        } catch (Exception e) {
            // Capture integration, network, or decoding issues
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication rejection error: " + e.getMessage());
        }
    }
}
