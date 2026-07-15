package com.carservice.userprofile.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UserProfile {
    
    private Long id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required") 
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(?i)(admin|mechanic|customer)$", 
             message = "Role can only be 'admin', 'mechanic', or 'customer'")
    private String role;

    private String preferences;

    // Default constructor for Jackson
    public UserProfile() {}

    // DTO Conversion Constructors
    public UserProfile(CustomerProfile c) {
        this.id = c.getId();
        this.username = c.getUsername();
        this.password = c.getPassword();
        this.role = c.getRole();
        this.preferences = c.getPreferences();
    }

    public UserProfile(MechanicProfile m) {
        this.id = m.getId();
        this.username = m.getUsername();
        this.password = m.getPassword();
        this.role = m.getRole();
        this.preferences = m.getPreferences();
    }

    public UserProfile(AdminProfile a) {
        this.id = a.getId();
        this.username = a.getUsername();
        this.password = a.getPassword();
        this.role = a.getRole();
        this.preferences = a.getPreferences();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { 
        this.role = role;
        normalizeRole();
    }

    public void normalizeRole() {
        if (this.role != null) {
            this.role = this.role.toLowerCase().trim();
        }
    }

    public String getPreferences() { return preferences; }
    public void setPreferences(String preferences) { this.preferences = preferences; }

    // Custom helper getter to assign each customer a userID format
    public String getUserID() {
        if ("customer".equalsIgnoreCase(this.role) && this.id != null) {
            return "USR-" + this.id;
        }
        return null;
    }

    // Custom helper getter to assign each mechanic a mechID format
    public String getMechID() {
        if ("mechanic".equalsIgnoreCase(this.role) && this.id != null) {
            return "MCH-" + this.id;
        }
        return null;
    }
}
