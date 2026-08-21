package com.example.candidate.dto.response;

import java.time.Instant;

public record CandidateCvResponse(
        Long id,
        String originalFilename,
        String fileType,
        Long fileSizeBytes,
        boolean parsed,
        String parsedData,
        Instant uploadedAt
) {
}
