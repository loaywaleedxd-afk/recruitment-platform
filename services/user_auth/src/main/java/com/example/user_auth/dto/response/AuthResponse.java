package com.example.user_auth.dto.response;

import lombok.Builder;

@Builder
public record AuthResponse(
        String accessToken, //3shan a authenticate en aluser da lyh access wala la
        String refreshToken, //ka2nha accesstoken tawylt almadaa
        long expiresInMs //m7tagha 3shan a3rf alaccesstoken ht expire emta
) { }
