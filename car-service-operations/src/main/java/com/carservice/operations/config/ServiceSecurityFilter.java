package com.carservice.operations.config;

import com.carservice.operations.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ServiceSecurityFilter implements Filter {

    private final JwtUtil jwtUtil;

    public ServiceSecurityFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Handle CORS preflight OPTIONS requests with 200 OK and CORS headers
        if (method.equalsIgnoreCase("OPTIONS")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 2. Allow Swagger UI documentation and public health checks
        if (path.contains("/swagger-ui") || 
            path.contains("/v3/api-docs") || 
            path.contains("/webjars") ||
            path.endsWith("/actuator/health")) {
            chain.doFilter(req, res);
            return;
        }

        // 3. Require valid JWT Bearer token or Gateway authentication header for ALL other endpoints
        String authHeader = request.getHeader("Authorization");
        String gatewayRoleHeader = request.getHeader("X-Authenticated-Role");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!jwtUtil.isTokenValid(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Service-Level Security: Invalid or expired JWT token.");
                return;
            }
        } else if (gatewayRoleHeader == null || gatewayRoleHeader.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Service-Level Security: Unauthorized direct access attempt detected.");
            return;
        }

        chain.doFilter(req, res);
    }
}
