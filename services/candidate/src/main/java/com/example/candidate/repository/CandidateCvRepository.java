package com.example.candidate.repository;

import com.example.candidate.entity.CandidateCv;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidateCvRepository extends JpaRepository<CandidateCv, Long> {

    //bttla3 agdad candidates b alcv bta3hom
    List<CandidateCv> findByCandidateIdcvNewest(Long candidateId);
    //btshof al cv at3amlo analyzation wala lsa
    List<CandidateCv> findByParsedFalse();
    //almain use bta3ha 3shan a3d kam cv ll candidate
    long countByCandidateId(Long candidateId);
}