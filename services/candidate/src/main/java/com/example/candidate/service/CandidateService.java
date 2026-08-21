package com.example.candidate.service;

import com.example.candidate.dto.request.CandidateSearchRequest;
import com.example.candidate.dto.request.CreateCandidateRequest;
import com.example.candidate.dto.request.UpdateCandidateRequest;
import com.example.candidate.dto.request.UpdateCandidateStatusRequest;
import com.example.candidate.dto.response.CandidateResponse;
import com.example.candidate.dto.response.CandidateSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CandidateService {

    CandidateResponse create(CreateCandidateRequest request);

    CandidateResponse getById(Long id);

    Page<CandidateSummaryResponse> search(CandidateSearchRequest filters, Pageable pageable);

    CandidateResponse update(Long id, UpdateCandidateRequest request);

    CandidateResponse changeStatus(Long id, UpdateCandidateStatusRequest request);

    List<String> listAllSkills();

    void delete(Long id);
}
