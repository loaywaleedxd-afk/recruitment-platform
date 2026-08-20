package com.example.user_auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "plz enter the Email can't be blank")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "plz enter the password")
        String password
) {
}