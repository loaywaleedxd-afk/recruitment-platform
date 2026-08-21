package com.example.candidate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "candidate_cvs")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateCv {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "parsed_data", length = 1000)
    private String parsedData;

    @Column(name = "is_parsed", nullable = false)
    private boolean parsed = false;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    public CandidateCv(String filePath, String originalFilename, String fileType, Long fileSizeBytes) {
        this.filePath = filePath;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.fileSizeBytes = fileSizeBytes;
    }

    @PrePersist
    void onCreate() {
        if (uploadedAt == null) uploadedAt = Instant.now();
    }

    }
