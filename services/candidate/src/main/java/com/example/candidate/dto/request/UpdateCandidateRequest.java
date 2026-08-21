package com.example.candidate.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateCandidateRequest(

        @Size(max = 150, message = "Full name must be at most 150 characters")
        String fullName,

        @Pattern(
                regexp = "^(\\+\\d{7,12}|\\d{7,12})?$",
                message = "Phone must be 7–12 digits, optionally prefixed with +"
        )

        String phone,

        @Size(max = 60, message = "Source must be at most 60 characters")
        String source,

        Set<@Size(max = 100, message = "max skill  100 characters") String> skills,

        Set<@Size(max = 60, message = "Tag must be at most 60 characters") String> tags
) {
}
