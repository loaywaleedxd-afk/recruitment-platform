package com.example.candidate.service;

import com.example.candidate.dto.response.CandidateCvResponse;
import com.example.candidate.dto.response.CvDownload;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CandidateCvService {

    CandidateCvResponse upload(Long candidateId, MultipartFile file);

    List<CandidateCvResponse> listByCandidate(Long candidateId);

    CvDownload download(Long candidateId, Long cvId);

    void delete(Long candidateId, Long cvId);
}
