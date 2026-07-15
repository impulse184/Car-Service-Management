package com.carservice.gateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

@Component
public class JwtAuthenticationFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private final JwtUtil jwtUtil;

    // Constructor injection
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public ServerResponse filter(ServerRequest request, HandlerFunction<ServerResponse> next) throws Exception {
        // Extract HTTP URI path
        String path = request.path();
        // Extract HTTP method name
        String method = request.method().name();

        // Bypass security for CORS preflight, login, registration, and swagger docs
        if (method.equalsIgnoreCase("OPTIONS") ||
            path.contains("/auth/login") ||  
            (path.contains("/users/userprofile") && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("GET"))) ||
            path.contains("/swagger-ui") || 
            path.contains("/v3/api-docs") || 
            path.contains("/webjars")) {
            
            return next.handle(request);
        }

        // authorization header
        // Get the authorization value from the request
        String authHeader = request.headers().firstHeader(HttpHeaders.AUTHORIZATION);
        // If header is missing
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // Throw 401 by global exception
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        // Filter out the first 7 characters ("Bearer ") to get JWT
        String token = authHeader.substring(7);

        // token expiration check
        if (jwtUtil.isTokenExpired(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token has expired");
        }

        // get username from token
        String username = jwtUtil.extractUsername(token);
        // get role from token
        String role = jwtUtil.extractRole(token);

        // Customer can only view records (GET) — no create, update, or delete
        if ("customer".equalsIgnoreCase(role) && path.contains("/carservice") && !method.equalsIgnoreCase("GET")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Customers can only view their service records.");
        }

        // Mechanic can only GET records or update status via PUT
        if ("mechanic".equalsIgnoreCase(role)) {
            if (method.equalsIgnoreCase("DELETE")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Mechanics are not authorized to delete records.");
            }
            if (method.equalsIgnoreCase("POST")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Mechanics are not authorized to create records.");
            }
            // Mechanics can only PUT for updating status
            if (method.equalsIgnoreCase("PUT") && !path.contains("/status")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied: Mechanics can only update operational status fields.");
            }
        }

        // Admin can change and access anything 

        // Extract user ID from token
        Long userId = jwtUtil.extractUserId(token);

        // Create new immutable request builder
        ServerRequest.Builder builder = ServerRequest.from(request)
                // Inject the username header
                .header("X-Authenticated-User", username)
                // Inject the role header
                .header("X-Authenticated-Role", role);
        
        if (userId != null) {
            // Inject the user ID header only if not null
            builder.header("X-Authenticated-Id", String.valueOf(userId));
        }

        ServerRequest authenticatedRequest = builder.build();

        // Pass the request to the next target
        return next.handle(authenticatedRequest);
    }
}
