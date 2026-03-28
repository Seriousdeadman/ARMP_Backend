package com.university.backend.hr.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String skillsAndExperience;

    @Column
    private String fileName;

    @Column
    private String fileContentType;

    @Column
    private Long fileSizeBytes;

    @Column
    private String fileStoragePath;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    @JsonIgnore
    private Candidate candidate;
}
