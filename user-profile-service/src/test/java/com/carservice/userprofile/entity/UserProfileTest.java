package com.carservice.userprofile.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserProfileTest {

    @Test
    public void normalizeRole_HappyPath_TrimsAndConvertsToLowerCase() {
        UserProfile profile = new UserProfile();
        profile.setRole("  AdMin  ");
        profile.normalizeRole();
        assertEquals("admin", profile.getRole());
    }

    @Test
    public void normalizeRole_NullPath_DoesNotThrowException() {
        UserProfile profile = new UserProfile();
        profile.setRole(null);
        assertDoesNotThrow(profile::normalizeRole);
        assertNull(profile.getRole());
    }
}
