package com.example.candidate.repository;

import com.example.candidate.entity.Candidate;
import com.example.candidate.entity.CandidateStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    //almain usage bta3tha en lw fy bulk import lusers myb2ash fy duplicates
    Optional<Candidate> findByEmail(String email);
    //byaked eno almail msh mwgod abl kda
    boolean existsByEmail(String email);
    //bttla3 candidates b status mo3yna w pageable bttla3 kam row per page ttla3
    Page<Candidate> findByStatus(CandidateStatus status, Pageable pageable);
    // ast5damha eno lw fy haga case sensitive t ignorha
    List<Candidate> findByFullNameOrEmail(
            String name, String email);

    // al query dy harfyn search engine b kol alanwa3 w alfalatr en kan mn tag aw skill aw status w lower aw upper case ignorince ll mail aw alname
    @Query("""
        select distinct c from Candidate c
        left join c.tags t
        left join c.skills s
        where (cast(:q as string) is null
               or lower(c.fullName) like lower(concat('%', cast(:q as string), '%'))
               or lower(c.email)    like lower(concat('%', cast(:q as string), '%')))
          and (cast(:status as string) is null or c.status = :status)
          and (cast(:tag as string) is null or lower(t.name) = lower(cast(:tag as string)))
          and (cast(:skill as string) is null or s.skillName = lower(cast(:skill as string)))
        """)
    Page<Candidate> search(@Param("q") String q,
                           @Param("status") CandidateStatus status,
                           @Param("tag") String tag,
                           @Param("skill") String skill,
                           Pageable pageable);
    //al query dy mn ala5er btsearch b aldatabase id w btrga3 kol haga ll candidate
    @Query("""
            select distinct c from Candidate c
            left join fetch c.tags
            left join fetch c.skills
            where c.id = :id
            """)
    // 3shan lw al id msh mwgod myb2sh fy error yrga3 eno msh mwgod
    Optional<Candidate> findByIdWithDetails(@Param("id") Long id);
}