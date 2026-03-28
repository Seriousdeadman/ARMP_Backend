package com.university.backend.hr.controllers;

import com.university.backend.hr.entities.Cv;
import com.university.backend.hr.services.CandidateService;
import com.university.backend.hr.services.RecruitmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CandidateControllerTest {

    @Mock
    private CandidateService candidateService;

    @Mock
    private RecruitmentService recruitmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new CandidateController(candidateService, recruitmentService)
        ).build();
    }

    @Test
    void getCvFileMetadata_returnsNoFileWhenCandidateHasNoCv() throws Exception {
        when(candidateService.findCvForCandidateOrNull("candidate-1")).thenReturn(null);

        mockMvc.perform(get("/api/hr/candidates/candidate-1/cv-file/metadata"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateId").value("candidate-1"))
                .andExpect(jsonPath("$.filePresent").value(false));
    }

    @Test
    void uploadCvFile_returnsMetadata() throws Exception {
        Cv cv = Cv.builder()
                .fileName("resume.pdf")
                .fileContentType("application/pdf")
                .fileSizeBytes(11L)
                .fileStoragePath("/tmp/resume.pdf")
                .build();

        when(candidateService.uploadCvFile(eq("candidate-1"), org.mockito.ArgumentMatchers.any())).thenReturn(cv);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "hello world".getBytes()
        );

        mockMvc.perform(multipart("/api/hr/candidates/candidate-1/cv-file").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.candidateId").value("candidate-1"))
                .andExpect(jsonPath("$.filePresent").value(true))
                .andExpect(jsonPath("$.fileName").value("resume.pdf"));

        verify(candidateService).uploadCvFile(eq("candidate-1"), org.mockito.ArgumentMatchers.any());
    }
}
