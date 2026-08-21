package com.example.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private static final List<String> ROLE_CLAIMS = List.of("roles", "role", "authorities");

    private final SecretKey key;
    private final String issuer;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    public Claims validateAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public List<String> extractRoles(Claims claims) {
        Object raw = ROLE_CLAIMS.stream()
                .map(claims::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        if (raw == null) {
            log.warn("Token for {} has none of the claims {}", claims.getSubject(), ROLE_CLAIMS);
            return List.of();
        }

        List<String> names = switch (raw) {
            case Collection<?> c -> c.stream().map(String::valueOf).toList();
            case String s -> List.of(s.split("\\s*,\\s*"));
            default -> List.of(String.valueOf(raw));
        };

        return names.stream()
                .map(n -> n.trim().toUpperCase(Locale.ROOT))
                .map(n -> n.startsWith("ROLE_") ? n.substring("ROLE_".length()) : n)
                .filter(n -> !n.isEmpty())
                .distinct()
                .toList();
    }
}
