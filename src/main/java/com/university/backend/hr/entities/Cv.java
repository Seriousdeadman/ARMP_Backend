package com.university.backend.hr.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "hr_cvs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cv {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(length = 36)
    private String id;

    /** Legacy rows may be NULL before backfill; portal still needs to load the CV row. */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String skillsAndExperience;

    @Column
    private String fileName;

    @Column
    private String fileContentType;

    @Column
    private Long fileSizeBytes;

    @Column(columnDefinition = "TEXT")
    private String fileStoragePath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    @JsonIgnore
    private Candidate candidate;
}
