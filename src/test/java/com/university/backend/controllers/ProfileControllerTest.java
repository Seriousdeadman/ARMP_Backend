package com.university.backend.controllers;

import com.university.backend.entities.User;
import com.university.backend.entities.UserStatistics;
import com.university.backend.enums.UserRole;
import com.university.backend.repositories.RefreshTokenRepository;
import com.university.backend.repositories.SessionRepository;
import com.university.backend.repositories.UserRepository;
import com.university.backend.repositories.UserStatisticsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserStatisticsRepository userStatisticsRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private static User testPrincipal() {
        return User.builder()
                .id("user-1")
                .firstName("HR")
                .lastName("Staff")
                .email("hr.staff@test.com")
                .passwordHash("hash")
                .phone("1")
                .role(UserRole.LOGISTICS_STAFF)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .totalLoginCount(0)
                .isTwoFactorEnabled(false)
                .build();
    }

    @BeforeEach
    void setUp() {
        User u = testPrincipal();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(u, null, u.getAuthorities()));
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ProfileController(
                        sessionRepository,
                        userStatisticsRepository,
                        refreshTokenRepository,
                        userRepository,
                        passwordEncoder
                )
        ).setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyStatistics_returnsDtoWhenRowExists() throws Exception {
        User u = testPrincipal();
        UserStatistics stats = UserStatistics.builder()
                .id("stat-1")
                .user(u)
                .totalLoginCount(5)
                .totalHoursConnected(2.5)
                .averageSessionDuration(30.0)
                .totalDevicesUsed(2)
                .mostUsedDevice("Chrome")
                .mostVisitedPage("/dashboard")
                .peakUsageHour(14)
                .lastUpdated(LocalDateTime.of(2025, 3, 1, 12, 0))
                .build();

        when(userStatisticsRepository.findByUserId("user-1")).thenReturn(Optional.of(stats));

        mockMvc.perform(get("/api/profile/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("stat-1"))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.totalLoginCount").value(5))
                .andExpect(jsonPath("$.totalHoursConnected").value(2.5))
                .andExpect(jsonPath("$.averageSessionDuration").value(30.0))
                .andExpect(jsonPath("$.totalDevicesUsed").value(2))
                .andExpect(jsonPath("$.mostUsedDevice").value("Chrome"))
                .andExpect(jsonPath("$.peakUsageHour").value(14));
    }

    @Test
    void getMyStatistics_returnsZerosWhenNoRow() throws Exception {
        when(userStatisticsRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/profile/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.totalLoginCount").value(0))
                .andExpect(jsonPath("$.totalHoursConnected").value(0.0))
                .andExpect(jsonPath("$.averageSessionDuration").value(0.0))
                .andExpect(jsonPath("$.totalDevicesUsed").value(0));
    }
}
