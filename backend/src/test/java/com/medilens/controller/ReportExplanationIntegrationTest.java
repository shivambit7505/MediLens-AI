package com.medilens.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilens.client.*;
import com.medilens.dto.AuthResponse;
import com.medilens.dto.RegisterRequest;
import com.medilens.dto.report.ReportResponseDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReportExplanationIntegrationTest {

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
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        samplePngBytes = baos.toByteArray();

        Mockito.when(aiServiceClient.processOcrPage(anyString(), anyInt()))
                .thenReturn(new AiOcrResponse(1, "Glucose 145 mg/dL Potassium 6.2 mmol/L", 0.98, "paddleocr"));

        List<AiMeasurementDto> mockMeasurements = List.of(
                new AiMeasurementDto(
                        "Glucose",
                        "Glucose",
                        "2345-7",
                        "145 mg/dL",
                        BigDecimal.valueOf(145.0),
                        "mg/dL",
                        BigDecimal.valueOf(145.0),
                        "mg/dL",
                        "70-99",
                        "HIGH",
                        BigDecimal.valueOf(0.95),
                        "Glucose 145 mg/dL",
                        "ADA 2024"
                ),
                new AiMeasurementDto(
                        "Potassium",
                        "Potassium",
                        "2823-3",
                        "6.2 mmol/L",
                        BigDecimal.valueOf(6.2),
                        "mmol/L",
                        BigDecimal.valueOf(6.2),
                        "mmol/L",
                        "3.5-5.2",
                        "CRITICAL",
                        BigDecimal.valueOf(0.97),
                        "Potassium 6.2 mmol/L",
                        "Mayo Clinic 2024"
                )
        );

        Mockito.when(aiServiceClient.extractAndValidateBiomarkers(anyString(), anyDouble(), anyString()))
                .thenReturn(new AiExtractionResponse(2, mockMeasurements));

        AiRagResponse mockRagResponse = new AiRagResponse(
                "rep-uuid",
                "Analysis of 2 laboratory biomarkers shows 2 result(s) with out-of-range or critical status.",
                List.of(
                        new AiRagResponse.AiFindingExplanation(
                                "Glucose",
                                "145.0 mg/dL",
                                "HIGH",
                                "70-99",
                                "Your observed Glucose (145.0 mg/dL) is elevated [Source: ADA-2024-GLUCOSE].",
                                "Elevated fasting blood sugar suggests insulin resistance.",
                                "Dietary changes and exercise recommended.",
                                List.of("ADA-2024-GLUCOSE")
                        ),
                        new AiRagResponse.AiFindingExplanation(
                                "Potassium",
                                "6.2 mmol/L",
                                "CRITICAL",
                                "3.5-5.2",
                                "CRITICAL VALUE: Your observed Potassium (6.2 mmol/L) is in a critical range [Source: MAYO-ELECTROLYTES-POTASSIUM].",
                                "Hyperkalemia may lead to cardiac conduction abnormalities.",
                                "Immediate evaluation recommended.",
                                List.of("MAYO-ELECTROLYTES-POTASSIUM")
                        )
                ),
                List.of("What immediate medical steps should I take regarding my critical Potassium level?"),
                "🚨 CRITICAL CLINICAL ALERT: One or more biomarkers are in a critical range requiring urgent medical evaluation.",
                "This information is for educational purposes only and does not constitute medical advice or diagnosis.",
                List.of(
                        new AiRagResponse.AiEvidenceSource("ADA-2024-GLUCOSE", "Fasting Plasma Glucose", "ADA 2024", "METABOLIC"),
                        new AiRagResponse.AiEvidenceSource("MAYO-ELECTROLYTES-POTASSIUM", "Serum Potassium Homeostasis", "Mayo Clinic", "ELECTROLYTE")
                ),
                true
        );

        Mockito.when(aiServiceClient.generateReportExplanation(any(AiRagRequest.class)))
                .thenReturn(mockRagResponse);

        // Register User 1
        String email1 = "ragpatient1_" + UUID.randomUUID() + "@medilens.com";
        RegisterRequest req1 = new RegisterRequest(
                email1, "SecurePass123!", "Patient", "One",
                java.time.LocalDate.of(1990, 1, 1), "FEMALE", com.medilens.model.Role.ROLE_PATIENT
        );
        String res1 = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        authTokenUser1 = objectMapper.readValue(res1, AuthResponse.class).getAccessToken();

        // Register User 2
        String email2 = "ragpatient2_" + UUID.randomUUID() + "@medilens.com";
        RegisterRequest req2 = new RegisterRequest(
                email2, "SecurePass123!", "Patient", "Two",
                java.time.LocalDate.of(1985, 5, 12), "MALE", com.medilens.model.Role.ROLE_PATIENT
        );
        String res2 = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        authTokenUser2 = objectMapper.readValue(res2, AuthResponse.class).getAccessToken();
    }

    @Test
    @DisplayName("GET /api/v1/reports/{id}/explanation without auth returns 401")
    void testExplanationWithoutAuthFails() throws Exception {
        mockMvc.perform(get("/api/v1/reports/" + UUID.randomUUID() + "/explanation"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/reports/{id}/explanation returns evidence-grounded findings with citations and disclaimers")
    void testExplanationSuccess() throws Exception {
        // Upload report as User 1
        MockMultipartFile file = new MockMultipartFile(
                "file", "metabolic_panel.png", "image/png", samplePngBytes
        );

        String uploadRes = mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ReportResponseDto report = objectMapper.readValue(uploadRes, ReportResponseDto.class);
        UUID reportId = report.id();

        // Request explanation
        mockMvc.perform(get("/api/v1/reports/" + reportId + "/explanation")
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId", is(reportId.toString())))
                .andExpect(jsonPath("$.summary", containsString("laboratory biomarkers")))
                .andExpect(jsonPath("$.criticalAlert", containsString("CRITICAL CLINICAL ALERT")))
                .andExpect(jsonPath("$.disclaimer", containsString("educational purposes only")))
                .andExpect(jsonPath("$.safetyAuditPassed", is(true)))
                .andExpect(jsonPath("$.findings", hasSize(2)))
                .andExpect(jsonPath("$.findings[0].canonicalName", is("Glucose")))
                .andExpect(jsonPath("$.findings[0].sources[0]", is("ADA-2024-GLUCOSE")))
                .andExpect(jsonPath("$.findings[1].canonicalName", is("Potassium")))
                .andExpect(jsonPath("$.findings[1].status", is("CRITICAL")))
                .andExpect(jsonPath("$.citedSources", hasSize(2)))
                .andExpect(jsonPath("$.questionsForDoctor", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("Anti-IDOR: User 2 cannot access User 1's report explanation")
    void testExplanationAntiIdor() throws Exception {
        // Upload report as User 1
        MockMultipartFile file = new MockMultipartFile(
                "file", "user1_report.png", "image/png", samplePngBytes
        );

        String uploadRes = mockMvc.perform(multipart("/api/v1/reports/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + authTokenUser1))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ReportResponseDto report = objectMapper.readValue(uploadRes, ReportResponseDto.class);
        UUID reportId = report.id();

        // User 2 tries to access User 1's explanation -> 404
        mockMvc.perform(get("/api/v1/reports/" + reportId + "/explanation")
                        .header("Authorization", "Bearer " + authTokenUser2))
                .andExpect(status().isNotFound());
    }
}
