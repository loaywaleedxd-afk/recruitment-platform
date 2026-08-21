package com.example.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator"
    );

    private static final List<String> ROLE_CLAIMS = List.of("roles", "role", "authorities");

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-Auth-User-Id";
    private static final String ROLES_HEADER = "X-Auth-Roles";

    private final SecretKey key;
    private final String issuer;

    public JwtAuthenticationFilter(@Value("${app.jwt.secret}") String secret,
                                   @Value("${app.jwt.issuer}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange.mutate().request(stripped(request)).build());
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return deny(exchange, "Missing or malformed Authorization header");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .requireIssuer(issuer)
                    .clockSkewSeconds(30)
                    .build()
                    .parseSignedClaims(authHeader.substring(BEARER_PREFIX.length()).trim())
                    .getPayload();

            List<String> roles = extractRoles(claims);

            ServerHttpRequest mutated = request.mutate()
                    .header(USER_ID_HEADER, claims.getSubject())
                    .header(ROLES_HEADER, String.join(",", roles))
                    .build();

            log.debug("Authenticated {} roles={} on {} {}",
                    claims.getSubject(), roles, request.getMethod(), path);

            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Rejected token on {} {}: {}", request.getMethod(), path, e.getMessage());
            return deny(exchange, "Invalid or expired token");
        }
    }

    private List<String> extractRoles(Claims claims) {
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

    private ServerHttpRequest stripped(ServerHttpRequest request) {
        return request.mutate()
                .headers(h -> {
                    h.remove(USER_ID_HEADER);
                    h.remove(ROLES_HEADER);
                })
                .build();
    }

    private Mono<Void> deny(ServerWebExchange exchange, String reason) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body = ("{\"status\":401,\"error\":\"" + reason + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
