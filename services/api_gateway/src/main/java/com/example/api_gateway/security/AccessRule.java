package com.example.api_gateway.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record AccessRule(PathPattern pattern, Set<HttpMethod> methods, Set<String> roles) {

    private static final PathPatternParser PARSER = PathPatternParser.defaultInstance;

    public static AccessRule any(String pattern, String... roles) {
        return new AccessRule(PARSER.parse(pattern), Set.of(), upper(roles));
    }

    public static AccessRule of(HttpMethod method, String pattern, String... roles) {
        return new AccessRule(PARSER.parse(pattern), Set.of(method), upper(roles));
    }

    public boolean matches(ServerHttpRequest request) {
        boolean methodOk = methods.isEmpty() || methods.contains(request.getMethod());
        return methodOk && pattern.matches(request.getPath().pathWithinApplication());
    }

    public boolean permits(Set<String> callerRoles) {
        return roles.isEmpty() || callerRoles.stream().anyMatch(roles::contains);
    }

    private static Set<String> upper(String... roles) {
        return Arrays.stream(roles)
                .map(r -> r.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String toString() {
        return (methods.isEmpty() ? "*" : methods) + " " + pattern
                + " -> " + (roles.isEmpty() ? "authenticated" : roles);
    }
}
