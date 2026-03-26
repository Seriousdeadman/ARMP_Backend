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
public class UserValidationResponse {

    private String userId;
    private String username;
    private String email;
    private UserRole role;
    private Boolean isValid;
}