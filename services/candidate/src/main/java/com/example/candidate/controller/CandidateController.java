package com.example.candidate.controller;

import com.example.candidate.dto.request.CandidateSearchRequest;
import com.example.candidate.dto.request.CreateCandidateRequest;
import com.example.candidate.dto.request.UpdateCandidateRequest;
import com.example.candidate.dto.request.UpdateCandidateStatusRequest;
import com.example.candidate.dto.response.CandidateCvResponse;
import com.example.candidate.dto.response.CandidateResponse;
import com.example.candidate.dto.response.CandidateSummaryResponse;
import com.example.candidate.dto.response.CvDownload;
import com.example.candidate.service.CandidateCvService;
import com.example.candidate.service.CandidateService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    private final CandidateService candidateService;
    private final CandidateCvService candidateCvService;

    public CandidateController(CandidateService candidateService,
                               CandidateCvService candidateCvService) {
        this.candidateService = candidateService;
        this.candidateCvService = candidateCvService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    //@PreAuthorize("hasRole('HR')")
    public CandidateResponse create(@Valid @RequestBody CreateCandidateRequest request) {
        return candidateService.create(request);
    }

    @GetMapping("/{id}")
    //@PreAuthorize("hasRole('HR')")
    public CandidateResponse getById(@PathVariable Long id) {
        return candidateService.getById(id);
    }

    @GetMapping
    //@PreAuthorize("hasRole('HR')")
    public Page<CandidateSummaryResponse> search(
            @Valid @ModelAttribute CandidateSearchRequest filters,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return candidateService.search(filters, pageable);
    }

    @GetMapping("/skills")
    //@PreAuthorize("hasRole('HR')")
    public List<String> listAllSkills() {
        return candidateService.listAllSkills();
    }

    @PutMapping("/{id}")
    //@PreAuthorize("hasRole('HR')")
    public CandidateResponse update(@PathVariable Long id,
                                    @Valid @RequestBody UpdateCandidateRequest request) {
        return candidateService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    //@PreAuthorize("hasRole('HR')")
    public CandidateResponse changeStatus(@PathVariable Long id,
                                          @Valid @RequestBody UpdateCandidateStatusRequest request) {
        return candidateService.changeStatus(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //@PreAuthorize("hasRole('HR')")
    public void delete(@PathVariable Long id) {
        candidateService.delete(id);
    }

    @PostMapping(path = "/{id}/cvs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    //@PreAuthorize("hasRole('HR')")
    public CandidateCvResponse uploadCv(@PathVariable Long id,
                                        @RequestPart("file") MultipartFile file) {
        return candidateCvService.upload(id, file);
    }

    @GetMapping("/{id}/cvs")
    //@PreAuthorize("hasRole('HR')")
    public List<CandidateCvResponse> listCvs(@PathVariable Long id) {
        return candidateCvService.listByCandidate(id);
    }

    @GetMapping("/{id}/cvs/{cvId}/download")
    //@PreAuthorize("hasRole('HR')")
    public ResponseEntity<Resource> downloadCv(@PathVariable Long id,
                                               @PathVariable Long cvId) {
        CvDownload download = candidateCvService.download(id, cvId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        download.contentType() != null
                                ? download.contentType()
                                : MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + download.filename() + "\"")
                .body(download.resource());
    }

    @DeleteMapping("/{id}/cvs/{cvId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    //@PreAuthorize("hasRole('HR')")
    public void deleteCv(@PathVariable Long id, @PathVariable Long cvId) {
        candidateCvService.delete(id, cvId);
    }
}