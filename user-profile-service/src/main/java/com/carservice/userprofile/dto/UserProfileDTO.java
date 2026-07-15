package com.carservice.userprofile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO containing incoming validations for creating user profiles.
public class UserProfileDTO {

    // Makes sure the username cannot be blank and between 3-100 characters
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 100, message = "Username must be between 3 and 100 characters")
    private String username;
    
    // Make sure the role cannot be blank
    @NotBlank(message = "Role is required")
    private String role;

    private String preferences;

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }
}
