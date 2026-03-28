package com.university.backend.services;

import com.university.backend.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret",
                "university_portal_secret_key_must_be_at_least_256_bits_long_for_hs256");
        ReflectionTestUtils.setField(jwtService, "accessExpiration", 900000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);
    }

    @Test
    void generateRefreshToken_tokensAreUniquePerIssuance() {
        User user = User.builder().id("user-1").build();
        String first = jwtService.generateRefreshToken(user);
        String second = jwtService.generateRefreshToken(user);
        assertThat(first).isNotEqualTo(second);
    }
}
