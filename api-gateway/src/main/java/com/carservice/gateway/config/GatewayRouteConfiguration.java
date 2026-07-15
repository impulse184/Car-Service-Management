package com.carservice.gateway.config;

import com.carservice.gateway.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;

import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRouteConfiguration {

    // Filter to handle JWT validation across incoming requests
    private final JwtAuthenticationFilter jwtFilter;

    // Constructor
    public GatewayRouteConfiguration(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // Route configuration for Car Service Operations.
    @Bean
    public RouterFunction<ServerResponse> carServiceOperationsRoute() {
        return GatewayRouterFunctions.route("car-service-operations-route")
                // Match both with and without trailing slash
                .route(path("/carservice").or(path("/carservice/**")), HandlerFunctions.http())
                // Validates the JWT token before passing the request
                .filter(jwtFilter) 
                // Load Balance distribution to "car-service-operations" instances
                .filter(LoadBalancerFilterFunctions.lb("car-service-operations")) 
                .build();
    }

    // Route configuration for Audit Service.
    @Bean
    public RouterFunction<ServerResponse> auditServiceRoute() {
        return GatewayRouterFunctions.route("audit-service-route")
                // Match both with and without trailing slash
                .route(path("/audits").or(path("/audits/**")), HandlerFunctions.http())
                .filter(jwtFilter)
                .filter(LoadBalancerFilterFunctions.lb("audit-service"))
                .build();
    }

    // Route configuration for User Profile Service
    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        return GatewayRouterFunctions.route("user-service-route")
                // Match both with and without trailing slash
                .route(path("/users").or(path("/users/**")), HandlerFunctions.http())
                .filter(jwtFilter) 
                .filter(LoadBalancerFilterFunctions.lb("user-profile-service")) 
                .build();
    }
}
