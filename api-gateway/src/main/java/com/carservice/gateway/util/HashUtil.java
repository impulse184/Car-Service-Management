package com.carservice.gateway.util; 

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

    // Salt text added(in this case a pepper)
    private static final String SALT = "CarServiceSuperSecretSaltValue123!";

    // Takes plain text and returns encrypted
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            throw new NullPointerException("Password to hash cannot be null");
        }
        try {
            // Adds password and salt
            String saltedPassword = plainPassword + SALT;
            
            // Gets and applies SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            
            // Build the final text string
            StringBuilder hexString = new StringBuilder();
            // Loops every byte one by one
            for (byte b : encodedHash) {
                // Byte to hexadecimal
                String hex = Integer.toHexString(0xff & b);
                // Add a zero if hex string is 1 digit
                if (hex.length() == 1) hexString.append('0');
                // Adds the hex together
                hexString.append(hex);
            }
            // Sends 64-character string
            return hexString.toString();
            
        // If no SHA-256 present
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password: SHA-256 algorithm not found", e);
        }
    }
}
