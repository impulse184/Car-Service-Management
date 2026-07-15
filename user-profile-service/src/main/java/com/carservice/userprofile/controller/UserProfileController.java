package com.carservice.userprofile.controller;

import com.carservice.userprofile.entity.UserProfile;
import com.carservice.userprofile.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/userprofile")
public class UserProfileController {

    private final UserProfileService userService;

    public UserProfileController(UserProfileService userService) {
        this.userService = userService;
    }
    
    // Maps to /users/userprofile via Gateway
    @PostMapping
    public ResponseEntity<UserProfile> createProfile(@Valid @RequestBody UserProfile profile) {
        return new ResponseEntity<>(userService.createProfile(profile), HttpStatus.CREATED);
    }

    // Maps to /users/userprofile via Gateway
    @GetMapping
    public ResponseEntity<List<UserProfile>> getAllProfiles() {
        return ResponseEntity.ok(userService.getAllProfiles());
    }

    // Maps to /users/userprofile/{id} via Gateway
    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getProfileById(id));
    }
}
