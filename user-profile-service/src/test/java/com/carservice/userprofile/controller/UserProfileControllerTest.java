package com.carservice.userprofile.controller;

import com.carservice.userprofile.entity.UserProfile;
import com.carservice.userprofile.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
public class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void createProfile_HappyPath_ReturnsCreatedStatus() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setUsername("testuser");
        profile.setPassword("password123");
        profile.setRole("admin");

        when(userService.createProfile(any(UserProfile.class))).thenReturn(profile);

        mockMvc.perform(post("/userprofile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("admin"));
    }

    @Test
    public void createProfile_FailurePath_ThrowsExceptionOnServiceError() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setUsername("testuser");
        profile.setPassword("password123");
        profile.setRole("admin");

        when(userService.createProfile(any(UserProfile.class))).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/userprofile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profile)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Service error"));
    }

    @Test
    public void getAllProfiles_HappyPath_ReturnsListOfProfiles() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setUsername("testuser");
        profile.setRole("admin");
        List<UserProfile> list = List.of(profile);

        when(userService.getAllProfiles()).thenReturn(list);

        mockMvc.perform(get("/userprofile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    public void getAllProfiles_EmptyPath_ReturnsEmptyListSuccessfully() throws Exception {
        when(userService.getAllProfiles()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/userprofile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void getProfileById_HappyPath_ReturnsProfileDetails() throws Exception {
        UserProfile profile = new UserProfile();
        profile.setId(1L);
        profile.setUsername("testuser");
        profile.setRole("admin");

        when(userService.getProfileById(1L)).thenReturn(profile);

        mockMvc.perform(get("/userprofile/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void getProfileById_FailurePath_ThrowsExceptionWhenUserNotFound() throws Exception {
        when(userService.getProfileById(99L)).thenThrow(new RuntimeException("User not found with ID: 99"));

        mockMvc.perform(get("/userprofile/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with ID: 99"));
    }
}
