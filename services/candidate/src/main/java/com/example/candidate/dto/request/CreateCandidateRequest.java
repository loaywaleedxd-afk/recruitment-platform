package com.example.candidate.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record CreateCandidateRequest(

        @NotBlank(message = "Full name is required")
        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @Size(max = 13, message = "Phone must be at most 13 characters")
        @Pattern(
                regexp = "^$|^\\+?[0-9]{7,12}$",
                message = "Phone must be 7-12 digits, optionally prefixed with +"
        )
        String phone,

        @Size(max = 60, message = "Source must be at most 60 characters")
        String source,

        Set<@NotBlank @Size(max = 100, message = "Skill must be at most 100 characters") String> skills,

        Set<@NotBlank @Size(max = 60, message = "Tag must be at most 60 characters") String> tags
) {
}