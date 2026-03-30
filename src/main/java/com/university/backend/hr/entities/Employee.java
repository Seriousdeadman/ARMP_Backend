package com.university.backend.hr.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.university.backend.entities.User;
import com.university.backend.hr.enums.EmployeeStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
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

    @Column(name = "promoted_at")
    private Instant promotedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoted_by_user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private User promotedBy;

    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
}
