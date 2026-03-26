package com.university.backend.controllers;

import com.university.backend.dto.*;
import com.university.backend.entities.User;
import com.university.backend.services.AuthService;
import com.university.backend.services.JwtService;
import com.university.backend.services.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal User user,
            @RequestParam String sessionId
    ) {
        authService.logout(user.getId(), sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<UserValidationResponse> validate(
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.substring(7);

        if (!jwtService.isTokenValid(token)) {
            return ResponseEntity.ok(
                    UserValidationResponse.builder()
                            .isValid(false)
                            .build()
            );
        }

        String userId = jwtService.extractUserId(token);
        User user = userDetailsService.loadUserById(userId);

        return ResponseEntity.ok(
                UserValidationResponse.builder()
                        .userId(user.getId())
                        .username(user.getFirstName() + " " + user.getLastName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .isValid(true)
                        .build()
        );
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<UserValidationResponse> getUserById(
            @PathVariable String id
    ) {
        User user = userDetailsService.loadUserById(id);

        return ResponseEntity.ok(
                UserValidationResponse.builder()
                        .userId(user.getId())
                        .username(user.getFirstName() + " " + user.getLastName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .isValid(true)
                        .build()
        );
    }
}