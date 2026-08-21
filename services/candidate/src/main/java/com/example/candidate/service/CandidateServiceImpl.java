package com.example.candidate.service;

import com.example.candidate.dto.request.CandidateSearchRequest;
import com.example.candidate.dto.request.CreateCandidateRequest;
import com.example.candidate.dto.request.UpdateCandidateRequest;
import com.example.candidate.dto.request.UpdateCandidateStatusRequest;
import com.example.candidate.dto.response.CandidateCvResponse;
import com.example.candidate.dto.response.CandidateResponse;
import com.example.candidate.dto.response.CandidateSummaryResponse;
import com.example.candidate.entity.Candidate;
import com.example.candidate.entity.CandidateCv;
import com.example.candidate.entity.CandidateSkill;
import com.example.candidate.entity.Tag;
import com.example.candidate.repository.CandidateRepository;
import com.example.candidate.repository.CandidateSkillRepository;
import com.example.candidate.repository.TagRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final TagRepository tagRepository;

    public CandidateServiceImpl(CandidateRepository candidateRepository,
                                CandidateSkillRepository candidateSkillRepository,
                                TagRepository tagRepository) {
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
        this.tagRepository = tagRepository;
    }
    @Override
    public CandidateResponse create(CreateCandidateRequest request) {
        String email = normaliseEmail(request.email());

        if (candidateRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A candidate with this email already exists");
        }

        Candidate candidate = new Candidate(request.fullName().trim(), email);
        candidate.setPhone(request.phone());
        candidate.setSource(request.source());
        candidate.setCreatedBy(currentUserId());

        applySkills(candidate, request.skills());
        applyTags(candidate, request.tags());

        return toResponse(candidateRepository.save(candidate));
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateResponse getById(Long id) {
        return toResponse(candidateRepository.findByIdWithDetails(id)
                .orElseThrow(this::notFound));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CandidateSummaryResponse> search(CandidateSearchRequest filters, Pageable pageable) {
        return candidateRepository.search(
                blankToNull(filters.q()),
                filters.status(),
                blankToNull(filters.tag()),
                blankToNull(filters.skill()),
                pageable
        ).map(this::toSummary);
    }

    @Override
    public CandidateResponse update(Long id, UpdateCandidateRequest request) {
        Candidate candidate = candidateRepository.findByIdWithDetails(id)
                .orElseThrow(this::notFound);

        if (request.fullName() != null) candidate.setFullName(request.fullName().trim());
        if (request.phone() != null) candidate.setPhone(request.phone());
        if (request.source() != null) candidate.setSource(request.source());

        if (request.skills() != null) {
            candidate.getSkills().clear();
            candidateRepository.flush();
            applySkills(candidate, request.skills());
        }

        if (request.tags() != null) {
            candidate.getTags().clear();
            applyTags(candidate, request.tags());
        }

        return toResponse(candidate);
    }

    @Override
    public CandidateResponse changeStatus(Long id, UpdateCandidateStatusRequest request) {
        Candidate candidate = candidateRepository.findByIdWithDetails(id)
                .orElseThrow(this::notFound);

        candidate.setStatus(request.status());

        return toResponse(candidate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> listAllSkills() {
        return candidateSkillRepository.findDistinctSkillNames();
    }

    @Override
    public void delete(Long id) {
        if (!candidateRepository.existsById(id)) {
            throw notFound();
        }
        candidateRepository.deleteById(id);
    }

    private void applySkills(Candidate candidate, Set<String> skills) {
        if (skills == null) return;

        skills.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(s -> s.trim().toLowerCase())
                .distinct()
                .forEach(name -> candidate.addSkill(new CandidateSkill(name)));
    }

    private void applyTags(Candidate candidate, Set<String> tags) {
        if (tags == null) return;

        tags.stream()
                .filter(t -> t != null && !t.isBlank())
                .map(String::trim)
                .distinct()
                .forEach(name -> {
                    Tag tag = tagRepository.findByNameIgnoreCase(name)
                            .orElseGet(() -> tagRepository.save(new Tag(name)));
                    candidate.addTag(tag);
                });
    }

    private CandidateResponse toResponse(Candidate candidate) {
        return new CandidateResponse(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getEmail(),
                candidate.getPhone(),
                candidate.getStatus(),
                candidate.getSource(),
                candidate.getCreatedBy(),
                candidate.getCreatedAt(),
                candidate.getUpdatedAt(),
                candidate.getSkills().stream()
                        .map(CandidateSkill::getSkillName)
                        .sorted()
                        .toList(),
                candidate.getTags().stream()
                        .map(Tag::getName)
                        .sorted()
                        .toList(),
                candidate.getCvs().stream()
                        .sorted(Comparator.comparing(CandidateCv::getUploadedAt).reversed())
                        .map(this::toCvResponse)
                        .toList()
        );
    }

    private CandidateSummaryResponse toSummary(Candidate candidate) {
        return new CandidateSummaryResponse(
                candidate.getId(),
                candidate.getFullName(),
                candidate.getEmail(),
                candidate.getPhone(),
                candidate.getStatus(),
                candidate.getSource(),
                candidate.getCreatedAt()
        );
    }

    private CandidateCvResponse toCvResponse(CandidateCv cv) {
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

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;

        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normaliseEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Candidate not found");
    }
}
