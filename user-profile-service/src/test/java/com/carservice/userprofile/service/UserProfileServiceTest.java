package com.carservice.userprofile.service;

import com.carservice.userprofile.entity.*;
import com.carservice.userprofile.repository.*;
import com.carservice.userprofile.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserProfileServiceTest {

    @Mock
    private CustomerProfileRepository customerRepository;

    @Mock
    private MechanicProfileRepository mechanicRepository;

    @Mock
    private AdminProfileRepository adminRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void createProfile_HappyPath_HashesPasswordAndSavesAdmin() {
        UserProfile input = new UserProfile();
        input.setUsername("newadmin");
        input.setPassword("password123");
        input.setRole("admin");

        AdminProfile saved = new AdminProfile();
        saved.setId(1L);
        saved.setUsername("newadmin");
        saved.setPassword(HashUtil.hashPassword("password123"));
        saved.setRole("admin");

        when(adminRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(customerRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(mechanicRepository.findByUsername("newadmin")).thenReturn(Optional.empty());
        when(adminRepository.save(any(AdminProfile.class))).thenReturn(saved);

        UserProfile result = userProfileService.createProfile(input);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("admin", result.getRole());
        assertEquals(HashUtil.hashPassword("password123"), result.getPassword());
        verify(adminRepository, times(1)).save(any(AdminProfile.class));
    }

    @Test
    public void createProfile_FailurePath_ThrowsExceptionWhenUsernameTaken() {
        UserProfile input = new UserProfile();
        input.setUsername("existinguser");
        input.setPassword("password123");
        input.setRole("customer");

        when(customerRepository.findByUsername("existinguser")).thenReturn(Optional.of(new CustomerProfile()));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userProfileService.createProfile(input);
        });

        assertEquals("Username 'existinguser' is already taken.", exception.getMessage());
        verify(customerRepository, never()).save(any(CustomerProfile.class));
    }

    @Test
    public void findByUsername_HappyPath_ReturnsUserProfileOptional() {
        CustomerProfile customer = new CustomerProfile();
        customer.setId(1L);
        customer.setUsername("testuser");
        customer.setRole("customer");

        when(customerRepository.findByUsername("testuser")).thenReturn(Optional.of(customer));
        when(mechanicRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(adminRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        Optional<UserProfile> result = userProfileService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("customer", result.get().getRole());
    }

    @Test
    public void findByUsername_EmptyPath_ReturnsEmptyOptionalWhenNotFound() {
        when(customerRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(mechanicRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(adminRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<UserProfile> result = userProfileService.findByUsername("nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    public void getAllProfiles_HappyPath_ReturnsListOfAllProfiles() {
        CustomerProfile customer = new CustomerProfile();
        customer.setId(1L);
        customer.setUsername("cust");
        customer.setRole("customer");

        MechanicProfile mechanic = new MechanicProfile();
        mechanic.setId(2L);
        mechanic.setUsername("mech");
        mechanic.setRole("mechanic");

        when(customerRepository.findAll()).thenReturn(List.of(customer));
        when(mechanicRepository.findAll()).thenReturn(List.of(mechanic));
        when(adminRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserProfile> result = userProfileService.getAllProfiles();

        assertEquals(2, result.size());
        assertEquals("cust", result.get(0).getUsername());
        assertEquals("mech", result.get(1).getUsername());
    }

    @Test
    public void getProfileById_HappyPath_ReturnsProfileDetails() {
        CustomerProfile customer = new CustomerProfile();
        customer.setId(1L);
        customer.setUsername("cust");
        customer.setRole("customer");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        UserProfile result = userProfileService.getProfileById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("customer", result.getRole());
    }

    @Test
    public void getProfileById_FailurePath_ThrowsExceptionWhenIdNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        when(mechanicRepository.findById(99L)).thenReturn(Optional.empty());
        when(adminRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userProfileService.getProfileById(99L);
        });

        assertEquals("User not found with ID: 99", exception.getMessage());
    }
}
