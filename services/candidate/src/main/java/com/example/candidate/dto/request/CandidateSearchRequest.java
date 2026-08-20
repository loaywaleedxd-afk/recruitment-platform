package com.example.candidate.dto.request;

import com.example.candidate.entity.CandidateStatus;
import jakarta.validation.constraints.Size;

public record CandidateSearchRequest(

        @Size(max = 255, message = "Search term must be at most 255 characters")
        String q,

        CandidateStatus status,

        @Size(max = 60, message = "Tag must be at most 60 characters")
        String tag,

        @Size(max = 100, message = "Skill must be at most 100 characters")
        String skill
) {
}