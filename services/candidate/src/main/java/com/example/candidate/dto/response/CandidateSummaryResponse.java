package com.example.candidate.dto.response;

import com.example.candidate.entity.CandidateStatus;

import java.time.Instant;


public record CandidateSummaryResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        CandidateStatus status,
        String source,
        Instant createdAt
) {
}