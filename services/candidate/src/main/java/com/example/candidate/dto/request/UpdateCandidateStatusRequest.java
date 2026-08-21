package com.example.candidate.dto.request;

import com.example.candidate.entity.CandidateStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCandidateStatusRequest(

        @NotNull(message = "Status is required")
        CandidateStatus status,

        @Size(max = 255, message = "Note must be at most 255 characters")
        String note
) {
}
