package com.carservice.userprofile.service;

import com.carservice.userprofile.entity.*;
import com.carservice.userprofile.repository.*;
import com.carservice.userprofile.util.HashUtil;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private final CustomerProfileRepository customerRepository;
    private final MechanicProfileRepository mechanicRepository;
    private final AdminProfileRepository adminRepository;

    public UserProfileService(CustomerProfileRepository customerRepository,
                              MechanicProfileRepository mechanicRepository,
                              AdminProfileRepository adminRepository) {
        this.customerRepository = customerRepository;
        this.mechanicRepository = mechanicRepository;
        this.adminRepository = adminRepository;
    }

    // Check if username is taken in any table
    private boolean isUsernameTaken(String username) {
        if (username == null) return false;
        String trimName = username.trim();
        return customerRepository.findByUsername(trimName).isPresent() ||
               mechanicRepository.findByUsername(trimName).isPresent() ||
               adminRepository.findByUsername(trimName).isPresent();
    }

    // User registration for all roles
    public UserProfile createProfile(UserProfile profile) {
        if (isUsernameTaken(profile.getUsername())) {
            throw new RuntimeException("Username '" + profile.getUsername() + "' is already taken.");
        }
        
        if (profile.getPassword() == null || profile.getPassword().isBlank()) {
            throw new RuntimeException("Password is required for all user accounts.");
        }

        // Encrypt password using SHA-256
        String secureHash = HashUtil.hashPassword(profile.getPassword());
        String role = profile.getRole();

        if ("customer".equalsIgnoreCase(role)) {
            CustomerProfile customer = new CustomerProfile();
            customer.setUsername(profile.getUsername().trim());
            customer.setPassword(secureHash);
            customer.setPreferences(profile.getPreferences());
            CustomerProfile saved = customerRepository.save(customer);
            return new UserProfile(saved);
        } else if ("mechanic".equalsIgnoreCase(role)) {
            MechanicProfile mechanic = new MechanicProfile();
            mechanic.setUsername(profile.getUsername().trim());
            mechanic.setPassword(secureHash);
            mechanic.setPreferences(profile.getPreferences());
            MechanicProfile saved = mechanicRepository.save(mechanic);
            return new UserProfile(saved);
        } else if ("admin".equalsIgnoreCase(role)) {
            AdminProfile admin = new AdminProfile();
            admin.setUsername(profile.getUsername().trim());
            admin.setPassword(secureHash);
            admin.setPreferences(profile.getPreferences());
            AdminProfile saved = adminRepository.save(admin);
            return new UserProfile(saved);
        } else {
            throw new RuntimeException("Invalid role '" + role + "' specified.");
        }
    }

    // Find by username, checking all tables
    public Optional<UserProfile> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String trimName = username.trim();

        Optional<CustomerProfile> customer = customerRepository.findByUsername(trimName);
        if (customer.isPresent()) {
            return Optional.of(new UserProfile(customer.get()));
        }

        Optional<MechanicProfile> mechanic = mechanicRepository.findByUsername(trimName);
        if (mechanic.isPresent()) {
            return Optional.of(new UserProfile(mechanic.get()));
        }

        Optional<AdminProfile> admin = adminRepository.findByUsername(trimName);
        if (admin.isPresent()) {
            return Optional.of(new UserProfile(admin.get()));
        }

        return Optional.empty();
    }

    // List all users from all tables
    public List<UserProfile> getAllProfiles() {
        List<UserProfile> all = new ArrayList<>();
        
        all.addAll(customerRepository.findAll().stream()
                .map(UserProfile::new)
                .collect(Collectors.toList()));
                
        all.addAll(mechanicRepository.findAll().stream()
                .map(UserProfile::new)
                .collect(Collectors.toList()));
                
        all.addAll(adminRepository.findAll().stream()
                .map(UserProfile::new)
                .collect(Collectors.toList()));
                
        return all;
    }

    // Get user by ID (checks Customer table first since operations only look up customers)
    public UserProfile getProfileById(Long id) {
        Optional<CustomerProfile> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return new UserProfile(customer.get());
        }

        Optional<MechanicProfile> mechanic = mechanicRepository.findById(id);
        if (mechanic.isPresent()) {
            return new UserProfile(mechanic.get());
        }

        Optional<AdminProfile> admin = adminRepository.findById(id);
        if (admin.isPresent()) {
            return new UserProfile(admin.get());
        }

        throw new RuntimeException("User not found with ID: " + id);
    }
}
