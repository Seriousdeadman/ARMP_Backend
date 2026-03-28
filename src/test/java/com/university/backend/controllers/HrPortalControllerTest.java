package com.university.backend.controllers;

import com.university.backend.entities.User;
import com.university.backend.enums.UserRole;
import com.university.backend.hr.dto.portal.ApplicationStatusResponse;
import com.university.backend.hr.dto.portal.CreateLeaveRequestDto;
import com.university.backend.hr.dto.portal.LeaveSummaryResponse;
import com.university.backend.hr.dto.portal.SubmittedLeaveRequestResponse;
import com.university.backend.hr.services.HrPortalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HrPortalControllerTest {

    @Mock
    private HrPortalService hrPortalService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HrPortalController(hrPortalService)).build();
    }

    private static User testPrincipal() {
        return User.builder()
                .id("user-1")
                .firstName("A")
                .lastName("B")
                .email("a@test.com")
                .passwordHash("hash")
                .phone("1")
                .role(UserRole.STUDENT)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .totalLoginCount(0)
                .isTwoFactorEnabled(false)
                .build();
    }

    @Test
    void getApplicationStatus_returnsBody() throws Exception {
        ApplicationStatusResponse body = ApplicationStatusResponse.builder()
                .candidateFound(true)
                .candidateStatus("PENDING")
                .message("Under review")
                .interviewScheduledAt(null)
                .interviewLocation(null)
                .build();
        when(hrPortalService.getApplicationStatus(any(User.class))).thenReturn(body);

        mockMvc.perform(get("/api/hr/portal/application-status")
                        .with(user(testPrincipal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateFound").value(true))
                .andExpect(jsonPath("$.message").value("Under review"));
    }

    @Test
    void getLeaveSummary_returnsBody() throws Exception {
        LeaveSummaryResponse body = LeaveSummaryResponse.builder()
                .employeeFound(true)
                .displayName("A B")
                .remainingLeaveDays(15)
                .build();
        when(hrPortalService.getLeaveSummary(any(User.class))).thenReturn(body);

        mockMvc.perform(get("/api/hr/portal/leave-summary")
                        .with(user(testPrincipal())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeFound").value(true))
                .andExpect(jsonPath("$.remainingLeaveDays").value(15));
    }

    @Test
    void submitLeaveRequest_returnsCreated() throws Exception {
        SubmittedLeaveRequestResponse body = new SubmittedLeaveRequestResponse(
                "lr-1",
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 3),
                "ANNUAL",
                "PENDING"
        );
        when(hrPortalService.submitLeaveRequest(any(User.class), any(CreateLeaveRequestDto.class)))
                .thenReturn(body);

        String json = """
                {"startDate":"2025-07-01","endDate":"2025-07-03","type":"ANNUAL"}
                """;

        mockMvc.perform(post("/api/hr/portal/leave-requests")
                        .with(user(testPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("lr-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
