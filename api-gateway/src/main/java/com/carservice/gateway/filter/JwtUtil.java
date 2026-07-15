package com.carservice.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

// Utility for JWT. Generates tokens and verifies signatures.
@Component
public class JwtUtil {

    // A secret string used as encryption key source (must be at least 256 bits for HMAC-SHA)
    private static final String SECRET_STRING = "local-development-only-jwt-signing-key-minimum-256-bits-length-requirement";
    
    // Converts bytes into SHA signing key
    private final Key key = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());
    
    // Token age set to 24 hours in milliseconds
    private static final long EXPIRATION_TIME = 86400000; // 24 Hours

    // Generates a signed JWT for an authenticated user.
    public String generateToken(Long userId, String username, String role) {
        // Map to hold custom claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        claims.put("id", userId);
        
        return Jwts.builder()
                .claims(claims)                    // Sets custom attribute (role)
                .subject(username)                 // Sets the username
                .issuedAt(new Date(System.currentTimeMillis())) // Sets timestamp
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Sets expiration timestamp
                .signWith(key)                     // Cryptographically signs payload
                .compact();                        // serializes JWT into dot separated string
    }

    // Extract the user ID from the JWT.
    public Long extractUserId(String token) {
        Object idVal = getClaims(token).get("id");
        if (idVal instanceof Number) {
            return ((Number) idVal).longValue();
        }
        return null;
    }

    // Extract the username claim from the JWT.
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    // Extract the role claim from the JWT.
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Check if the current time has surpassed the token's expiration date.
    public boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Verify and extract the JSON payload from JWT.
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key) // Uses the same key to verify the signature
                .build()
                .parseSignedClaims(token)                 // Validates format, signature, and expiration status
                .getPayload();                            // Extracts the body containing claims data
    }
}
