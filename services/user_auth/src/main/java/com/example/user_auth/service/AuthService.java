package com.example.user_auth.service;

import com.example.user_auth.dto.request.LoginRequest;
import com.example.user_auth.dto.request.RefreshTokenRequest;
import com.example.user_auth.dto.request.RegisterRequest;
import com.example.user_auth.dto.response.AuthResponse;
import com.example.user_auth.dto.response.UserResponse;
import com.example.user_auth.enums.Role;

import java.util.Set;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);

    UserResponse updateRoles(Long userId, Set<Role> roles);

    void logout(RefreshTokenRequest request);
}
