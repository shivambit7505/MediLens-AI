package com.medilens.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilens.client.*;
import com.medilens.dto.AuthResponse;
import com.medilens.dto.RegisterRequest;
import com.medilens.repository.ReportRepository;
import com.medilens.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ReportControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReportRepository reportRepository;

    @MockBean
    private AiServiceClient aiServiceClient;

    private String authTokenUser1;
    private String authTokenUser2;
    private byte[] samplePngBytes;

    @BeforeEach
    void setUp() throws Exception {
        // Generate valid 10x10 PNG bytes with magic number \x89PNG
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        samplePngBytes = baos.toByteArray();

        // Setup Mockito returns for AI Service
        Mockito.when(aiServiceClient.processOcrPage(anyString(), anyInt()))
                .thenReturn(new AiOcrResponse(1, "Hemoglobin 14.5 g/dL 13.8 - 17.2", 0.98, "paddleocr"));

        List<AiMeasurementDto> mockMeasurements = List.of(
                new AiMeasurementDto(
                        "Hemoglobin",
                        "Hemoglobin",
                        "718-7",
                        "14.5",
                        BigDecimal.valueOf(14.5),
                        "g/dL",
                        BigDecimal.valueOf(14.5),
                        "g/dL",
                        "13.8 - 17.2",
                        "NORMAL",
                        BigDecimal.valueOf(0.98),
                        "Hemoglobin 14.5 g/dL 13.8 - 17.2",
                        "Mayo Clinic 2024"
                )
        );

        Mockito.when(aiServiceClient.extractAndValidateBiomarkers(anyString(), anyDouble(), anyString()))
                .thenReturn(new AiExtractionResponse(1, mockMeasurements));

        // Register User 1
        String email1 = "patient1_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        RegisterRequest regReq1 = new RegisterRequest(email1, "SecurePass123!", "Jane", "Doe",
                java.time.LocalDate.of(1990, 1, 1), "FEMALE", com.medilens.model.Role.ROLE_PATIENT);
        String res1 = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        AuthResponse authRes1 = objectMapper.readValue(res1, AuthResponse.class);
        authTokenUser1 = authRes1.getAccessToken();

        // Register User 2
        String email2 = "patient2_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        RegisterRequest regReq2 = new RegisterRequest(email2, "SecurePass123!", "John", "Smith",
                java.time.LocalDate.of(1985, 5, 12), "MALE", com.medilens.model.Role.ROLE_PATIENT);
        String res2 = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        AuthResponse authRes2 = objectMapper.readValue(res2, AuthResponse.class);
        authTokenUser2 = authRes2.getAccessToken();
    }

    @Test
    @DisplayName("Should successfully upload, render, OCR, and extract measurements from report")
    void testUploadAndProcessReport() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "blood_test.png", "image/png", samplePngBytes
        );

        mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.originalFilename", is("blood_test.png")))
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.pageCount", is(1)))
                .andExpect(jsonPath("$.measurementCount", is(1)));
    }

    @Test
    @DisplayName("Should deduplicate report upload when same file is submitted twice")
    void testDeduplication() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "identical_report.png", "image/png", samplePngBytes
        );

        // Upload first time
        String firstResponse = mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String firstReportId = objectMapper.readTree(firstResponse).get("id").asText();

        // Upload identical file second time
        String secondResponse = mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String secondReportId = objectMapper.readTree(secondResponse).get("id").asText();

        // Must return the exact same report ID without re-running redundant processing
        org.junit.jupiter.api.Assertions.assertEquals(firstReportId, secondReportId);
    }

    @Test
    @DisplayName("Should list user's reports with pagination")
    void testListReports() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "list_test.png", "image/png", samplePngBytes
        );

        mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/reports")
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.content[0].originalFilename").isNotEmpty());
    }

    @Test
    @DisplayName("Should retrieve report details and measurements")
    void testGetReportDetailsAndMeasurements() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "cbc_details.png", "image/png", samplePngBytes
        );

        String uploadResponse = mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String reportId = objectMapper.readTree(uploadResponse).get("id").asText();

        // Fetch detail
        mockMvc.perform(get("/api/v1/reports/" + reportId)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(reportId)))
                .andExpect(jsonPath("$.pages", hasSize(1)))
                .andExpect(jsonPath("$.measurements", hasSize(1)))
                .andExpect(jsonPath("$.measurements[0].canonicalName", is("Hemoglobin")))
                .andExpect(jsonPath("$.measurements[0].status", is("NORMAL")));

        // Fetch measurements endpoint
        mockMvc.perform(get("/api/v1/reports/" + reportId + "/measurements")
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].observedValueNumeric", is(14.5)));
    }

    @Test
    @DisplayName("Anti-IDOR: User 2 cannot access User 1's report")
    void testAntiIdorProtection() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "private_report.png", "image/png", samplePngBytes
        );

        String uploadResponse = mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String reportIdUser1 = objectMapper.readTree(uploadResponse).get("id").asText();

        // User 2 attempts to view User 1's report
        mockMvc.perform(get("/api/v1/reports/" + reportIdUser1)
                        .header("Authorization", "Bearer " + authTokenUser2))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should reject report upload without JWT token")
    void testUnauthenticatedUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", samplePngBytes
        );

        mockMvc.perform(multipart("/api/v1/reports/upload").file(file))
                .andExpect(status().isUnauthorized());
    }
}
