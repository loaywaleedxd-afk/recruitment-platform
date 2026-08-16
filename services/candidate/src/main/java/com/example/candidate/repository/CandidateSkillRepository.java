package com.example.candidate.repository;

import com.example.candidate.entity.CandidateSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {
    // bsearch b alid 3shan atla3 kol alskills l candidate mo3yan
    List<CandidateSkill> findByCandidateId(Long candidateId);
    //3shan my7salsh duplicates ll skills
    boolean existsByCandidateIdAndSkillName(Long candidateId, String skillName);
    // bttla3lak al candidates aly 3andhom skill mo3yna
    List<CandidateSkill> findBySkillName(String skillName);
    // b retrive al outcome bta3 alnas aly 3andhom alskill almo3yna dy
    @Query("select distinct s.skillName from CandidateSkill s order by s.skillName")

    List<String> findDistinctSkillNames();

}