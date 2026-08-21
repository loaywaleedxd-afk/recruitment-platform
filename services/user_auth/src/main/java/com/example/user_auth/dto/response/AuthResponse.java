package com.example.user_auth.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInMs
) { }
