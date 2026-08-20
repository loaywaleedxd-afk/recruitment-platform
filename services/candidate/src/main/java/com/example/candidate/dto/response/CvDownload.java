package com.example.candidate.dto.response;

import org.springframework.core.io.Resource;

public record CvDownload(
        Resource resource,
        String filename,
        String contentType
) {
}