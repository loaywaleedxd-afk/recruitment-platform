package com.example.candidate.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(
        name = "candidate_skills",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_candidate_skill",
                columnNames = {"candidate_id", "skill_name"})
)
public class CandidateSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Column(name = "skill_name", nullable = false, length = 100)
    private String skillName;

    protected CandidateSkill() {
    }

    public CandidateSkill(String skillName) {
        this.skillName = skillName;
    }

    @PrePersist
    @PreUpdate
    void normalise() {
        if (skillName != null) skillName = skillName.trim().toLowerCase();
    }

    public Long getId() { return id; }
    public Candidate getCandidate() { return candidate; }
    public void setCandidate(Candidate candidate) { this.candidate = candidate; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidateSkill other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}