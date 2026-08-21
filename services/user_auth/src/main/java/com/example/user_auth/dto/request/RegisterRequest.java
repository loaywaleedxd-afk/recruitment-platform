package com.example.user_auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "plz enter your Full name ")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @NotBlank(message = "plz enter an Email")
        @Email(message = "Must be a valid email address")
        String email,

        @NotBlank(message = "plz enter a Password")
        @Size(min = 8, max = 50, message = "Password must be between 8 and 50 characters")
        String password
) {
}
