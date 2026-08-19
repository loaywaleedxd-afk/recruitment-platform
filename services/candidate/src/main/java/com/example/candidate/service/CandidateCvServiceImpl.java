package com.example.candidate.service;

import com.example.candidate.dto.response.CandidateCvResponse;
import com.example.candidate.dto.response.CvDownload;
import com.example.candidate.entity.Candidate;
import com.example.candidate.entity.CandidateCv;
import com.example.candidate.repository.CandidateCvRepository;
import com.example.candidate.repository.CandidateRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class CandidateCvServiceImpl implements CandidateCvService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private static final long MAX_CVS_PER_CANDIDATE = 5;

    private final CandidateRepository candidateRepository;
    private final CandidateCvRepository candidateCvRepository;
    private final Path storageRoot;

    public CandidateCvServiceImpl(CandidateRepository candidateRepository,
                                  CandidateCvRepository candidateCvRepository,
                                  @Value("${app.cv.storage-dir:uploads/cvs}") String storageDir) {
        this.candidateRepository = candidateRepository;
        this.candidateCvRepository = candidateCvRepository;
        this.storageRoot = Paths.get(storageDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void initStorageDirectory() {
        try {
            Files.createDirectories(storageRoot);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create CV storage directory", ex);
        }
    }

    @Override
    public CandidateCvResponse upload(Long candidateId, MultipartFile file) {
        validateUpload(file);

        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(this::candidateNotFound);

        if (candidateCvRepository.countByCandidateId(candidateId) >= MAX_CVS_PER_CANDIDATE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This candidate already has " + MAX_CVS_PER_CANDIDATE
                            + " CVs — delete one before uploading another");
        }

        String originalFilename = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "cv" : file.getOriginalFilename());

        String storedFilename = UUID.randomUUID() + extractExtension(originalFilename);
        Path storedPath = storageRoot.resolve(storedFilename).normalize();

        if (!storedPath.startsWith(storageRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        try (InputStream uploadStream = file.getInputStream()) {
            Files.copy(uploadStream, storedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not store the uploaded file", ex);
        }

        CandidateCv cv = new CandidateCv(
                storedPath.toString(),
                originalFilename,
                file.getContentType(),
                file.getSize());

        candidate.addCv(cv);

        try {
            return toResponse(candidateCvRepository.save(cv));
        } catch (RuntimeException ex) {
            // The transaction is rolling back — take the file with it, or
            // storage fills up with orphans no row points at.
            deleteFileQuietly(storedPath);
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateCvResponse> listByCandidate(Long candidateId) {
        if (!candidateRepository.existsById(candidateId)) {
            throw candidateNotFound();
        }

        return candidateCvRepository.findByCandidateIdcvNewest(candidateId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CvDownload download(Long candidateId, Long cvId) {
        CandidateCv cv = findCvOwnedByCandidate(candidateId, cvId);

        try {
            Resource fileResource = new UrlResource(
                    Paths.get(cv.getFilePath()).normalize().toUri());

            if (!fileResource.exists() || !fileResource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "The stored file is missing");
            }

            return new CvDownload(fileResource, cv.getOriginalFilename(), cv.getFileType());

        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not read the stored file", ex);
        }
    }

    @Override
    public void delete(Long candidateId, Long cvId) {
        CandidateCv cv = findCvOwnedByCandidate(candidateId, cvId);

        candidateCvRepository.delete(cv);
        deleteFileQuietly(Paths.get(cv.getFilePath()));
    }

    private CandidateCv findCvOwnedByCandidate(Long candidateId, Long cvId) {
        CandidateCv cv = candidateCvRepository.findById(cvId)
                .orElseThrow(this::cvNotFound);

        if (!cv.getCandidate().getId().equals(candidateId)) {
            throw cvNotFound();
        }

        return cv;
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "File must be 5MB or smaller");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Only PDF and Word documents are accepted");
        }
    }

    private CandidateCvResponse toResponse(CandidateCv cv) {
        return new CandidateCvResponse(
                cv.getId(),
                cv.getOriginalFilename(),
                cv.getFileType(),
                cv.getFileSizeBytes(),
                cv.isParsed(),
                cv.getParsedData(),
                cv.getUploadedAt()
        );
    }

    private String extractExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1 || lastDot == filename.length() - 1)
                ? ""
                : filename.substring(lastDot);
    }

    private void deleteFileQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private ResponseStatusException candidateNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
    }

    private ResponseStatusException cvNotFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "CV not found");
    }
}