package com.university.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column
    private String avatarUrl;

    @Column
    private String preferredSchedule;

    @Column
    private String preferredSpaces;

    @Column
    private String dietaryPreferences;

    @Column
    private String accessibilityNeeds;

    @Column
    private String cvFileUrl;
}