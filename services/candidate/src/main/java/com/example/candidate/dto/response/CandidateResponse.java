package com.example.candidate.dto.response;

import com.example.candidate.entity.CandidateStatus;

import java.time.Instant;
import java.util.List;

public record CandidateResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        CandidateStatus status,
        String source,
        Long createdBy,
        Instant createdAt,
        Instant updatedAt,
        List<String> skills,
        List<String> tags,
        List<CandidateCvResponse> cvs
) {
}
