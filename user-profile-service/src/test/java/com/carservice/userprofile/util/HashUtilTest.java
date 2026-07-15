package com.carservice.userprofile.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HashUtilTest {

    @Test
    public void hashPassword_HappyPath_ReturnsConsistentSixtyFourCharacterHex() {
        String input = "mypassword123";
        String hash1 = HashUtil.hashPassword(input);
        String hash2 = HashUtil.hashPassword(input);

        assertNotNull(hash1);
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2); // Consistent repeatable hash
        
        // Match regex for hex string
        assertTrue(hash1.matches("^[a-f0-9]{64}$"));
    }

    @Test
    public void hashPassword_FailurePath_ThrowsExceptionOnNullInput() {
        assertThrows(NullPointerException.class, () -> {
            HashUtil.hashPassword(null);
        });
    }
}
