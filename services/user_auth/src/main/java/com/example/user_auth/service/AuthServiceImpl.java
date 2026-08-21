package com.example.user_auth.service;

import com.example.user_auth.dto.request.LoginRequest;
import com.example.user_auth.dto.request.RefreshTokenRequest;
import com.example.user_auth.dto.request.RegisterRequest;
import com.example.user_auth.dto.response.AuthResponse;
import com.example.user_auth.dto.response.UserResponse;
import com.example.user_auth.entity.RefreshToken;
import com.example.user_auth.entity.User;
import com.example.user_auth.enums.Role;
import com.example.user_auth.repository.RefreshTokenRepository;
import com.example.user_auth.repository.UserRepository;
import com.example.user_auth.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Role DEFAULT_ROLE = Role.USER;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final Duration refreshTtl;

    public AuthServiceImpl(UserRepository userRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           @Value("${app.jwt.refresh-token-ttl-days:30}") long refreshTtlDays) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTtl = Duration.ofDays(refreshTtlDays);
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalise(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered: " + email);
        }

        User user = new User(
                request.fullName(),
                email,
                passwordEncoder.encode(request.password())
        );

        user.setRoles(Set.of(DEFAULT_ROLE));

        return issueTokens(userRepository.save(user));
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {

        String email = normalise(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found: " + email));

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new AccessDeniedException("Invalid refresh token"));

        if (!stored.isUsable()) {
            throw new AccessDeniedException("Refresh token is expired or revoked");
        }

        User user = stored.getUser();

        if (!user.isActive()) {
            throw new AccessDeniedException("Account is disabled");
        }

        stored.setRevoked(true);

        return issueTokens(user);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {

        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(token -> token.setRevoked(true));
    }

    @Override
    @Transactional
    public UserResponse updateRoles(Long userId, Set<Role> roles) {

        User target = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        String callerEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean targetIsAdmin = target.getRoles().contains(Role.ADMIN);
        boolean isSelf = target.getEmail().equalsIgnoreCase(normalise(callerEmail));

        if (targetIsAdmin && !isSelf) {
            throw new AccessDeniedException("Cannot modify the roles of another ADMIN");
        }

        boolean losingAdmin = targetIsAdmin && !roles.contains(Role.ADMIN);
        if (losingAdmin && userRepository.countByRolesContaining(Role.ADMIN) <= 1) {
            throw new IllegalStateException("Cannot remove the last remaining ADMIN");
        }

        target.setRoles(Set.copyOf(roles));

        refreshTokenRepository.revokeAllForUser(target.getId());

        return UserResponse.fromEntity(target);
    }

    private String normalise(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private AuthResponse issueTokens(User user) {

        RefreshToken refreshToken = refreshTokenRepository.save(
                new RefreshToken(generateRefreshTokenValue(), user, Instant.now().plus(refreshTtl)));

        return AuthResponse.builder()
                .accessToken(jwtService.generateToken(user))
                .refreshToken(refreshToken.getToken())
                .expiresInMs(jwtService.getAccessTokenTtlMillis())
                .build();
    }

    private String generateRefreshTokenValue() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return B64.encodeToString(bytes);
    }
}
