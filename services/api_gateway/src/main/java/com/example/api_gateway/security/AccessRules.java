package com.example.api_gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccessRules {

    private static final List<AccessRule> PUBLIC = List.of(
            AccessRule.of(HttpMethod.POST, "/api/auth/login"),
            AccessRule.of(HttpMethod.POST, "/api/auth/register"),
            AccessRule.any("/actuator/**")
    );

    private static final List<AccessRule> SECURED = List.of(

            AccessRule.of(HttpMethod.DELETE, "/api/candidates/{id}", "ADMIN"),
            AccessRule.of(HttpMethod.DELETE, "/api/candidates/{id}/cvs/{cvId}", "HR", "ADMIN"),

            AccessRule.any("/api/candidates/**", "HR", "ADMIN"),

            AccessRule.any("/api/auth/**")
    );

    public boolean isPublic(ServerHttpRequest request) {
        return PUBLIC.stream().anyMatch(r -> r.matches(request));
    }

    public Optional<AccessRule> findRule(ServerHttpRequest request) {
        return SECURED.stream().filter(r -> r.matches(request)).findFirst();
    }
}
