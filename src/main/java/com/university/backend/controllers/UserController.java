package com.university.backend.controllers;

import com.university.backend.entities.User;
import com.university.backend.entities.UserProfile;
import com.university.backend.repositories.UserProfileRepository;
import com.university.backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @GetMapping("/me")
    public ResponseEntity<User> getMyProfile(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateMyProfile(
            @AuthenticationPrincipal User user,
            @RequestBody User updatedUser
    ) {
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setPhone(updatedUser.getPhone());
        user.setDepartment(updatedUser.getDepartment());
        user.setPreferredLanguage(updatedUser.getPreferredLanguage());
        userRepository.save(user);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/me/profile")
    public ResponseEntity<UserProfile> getMyUserProfile(
            @AuthenticationPrincipal User user
    ) {
        UserProfile profile = userProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/me/profile")
    public ResponseEntity<UserProfile> updateMyUserProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UserProfile updatedProfile
    ) {
        UserProfile profile = userProfileRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setAvatarUrl(updatedProfile.getAvatarUrl());
        profile.setDietaryPreferences(updatedProfile.getDietaryPreferences());
        profile.setAccessibilityNeeds(updatedProfile.getAccessibilityNeeds());
        profile.setPreferredSchedule(updatedProfile.getPreferredSchedule());
        profile.setPreferredSpaces(updatedProfile.getPreferredSpaces());
        userProfileRepository.save(profile);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });
        return ResponseEntity.noContent().build();
    }
}