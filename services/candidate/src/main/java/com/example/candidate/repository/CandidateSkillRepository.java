package com.example.candidate.repository;

import com.example.candidate.entity.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {

    List<CandidateSkill> findByCandidateId(Long candidateId);

    boolean existsByCandidateIdAndSkillName(Long candidateId, String skillName);

    List<CandidateSkill> findBySkillName(String skillName);

    @Query("select distinct s.skillName from CandidateSkill s order by s.skillName")

    List<String> findDistinctSkillNames();

}
