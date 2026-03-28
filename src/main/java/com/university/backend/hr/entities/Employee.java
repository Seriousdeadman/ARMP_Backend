package com.university.backend.hr.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "hr_employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    @Builder.Default
    private Integer leaveBalance = 21;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "grade_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Grade grade;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;

    @OneToOne(optional = true)
    @JoinColumn(name = "source_candidate_id", unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "cv", "department", "hiredEmployee"})
    private Candidate sourceCandidate;
}
