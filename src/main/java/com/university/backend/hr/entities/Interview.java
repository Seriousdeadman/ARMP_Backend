package com.university.backend.hr.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.university.backend.hr.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hr_interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private LocalDateTime interviewDate;

    @Column(nullable = false)
    private String location;

    @Column
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "cv", "hiredEmployee"})
    private Candidate candidate;
}
