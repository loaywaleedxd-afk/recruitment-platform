package com.example.user_auth.controller;

import com.example.user_auth.dto.request.LoginRequest;
import com.example.user_auth.dto.request.RefreshTokenRequest;
import com.example.user_auth.dto.request.RegisterRequest;
import com.example.user_auth.dto.response.AuthResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request) {
        return null; // TODO
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return null; // TODO
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest request) {
        return null; // TODO
    }

    @PostMapping("/logout")
    public void logout(@RequestBody RefreshTokenRequest request) {
        // TODO
    }
}
