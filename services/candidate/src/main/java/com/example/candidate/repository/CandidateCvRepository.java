package com.example.candidate.repository;

import com.example.candidate.entity.CandidateCv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateCvRepository extends JpaRepository<CandidateCv, Long> {

    List<CandidateCv> findByCandidateIdOrderByUploadedAtDesc(Long candidateId);
    List<CandidateCv> findByParsedFalse();

    long countByCandidateId(Long candidateId);
}
