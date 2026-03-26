package com.university.backend.dto;

import com.university.backend.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
}