package com.university.backend.hr.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.university.backend.hr.enums.InterviewStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private LocalDateTime interviewDate;

    @Column(nullable = false)
    private String location;

    @Column
    private Integer score;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 32)
    private InterviewStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "candidate_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "cv", "hiredEmployee"})
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interviewer_id")
    @JsonIgnoreProperties({
            "hibernateLazyInitializer",
            "handler",
            "grade",
            "department",
            "sourceCandidate",
            "promotedBy"
    })
    private Employee interviewer;
}
