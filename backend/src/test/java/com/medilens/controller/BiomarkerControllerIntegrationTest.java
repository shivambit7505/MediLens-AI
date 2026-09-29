package com.medilens.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilens.dto.AuthResponse;
import com.medilens.dto.RegisterRequest;
import com.medilens.model.*;
import com.medilens.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;
import com.medilens.client.AiServiceClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BiomarkerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BiomarkerRepository biomarkerRepository;

    @MockBean
    private AiServiceClient aiServiceClient;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private MeasurementRepository measurementRepository;

    private String authToken;
    private User testUser;
    private Biomarker glucoseBiomarker;

    @BeforeEach
    void setUp() throws Exception {
        String email = "trend_user_" + UUID.randomUUID().toString().substring(0, 8) + "@medilens.ai";
        RegisterRequest regReq = new RegisterRequest(
                email, "SecurePassword123!", "Trend", "Patient",
                LocalDate.of(1985, 3, 20), "MALE", Role.ROLE_PATIENT
        );

        String regRes = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authRes = objectMapper.readValue(regRes, AuthResponse.class);
        authToken = authRes.getAccessToken();
        testUser = userRepository.findByEmail(email).orElseThrow();

        // Seed or find canonical glucose biomarker
        glucoseBiomarker = biomarkerRepository.findByCanonicalName("Fasting Blood Glucose")
                .orElseGet(() -> {
                    Biomarker b = new Biomarker();
                    b.setCanonicalName("Fasting Blood Glucose");
                    b.setCodeLoinc("1558-6");
                    b.setCategory("Metabolic Panel");
                    b.setStandardUnit("mg/dL");
                    return biomarkerRepository.save(b);
                });
    }

    @Test
    @DisplayName("Should retrieve canonical biomarker catalog")
    void testGetAllBiomarkers() throws Exception {
        mockMvc.perform(get("/api/v1/biomarkers")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[?(@.canonicalName == 'Fasting Blood Glucose')]").exists());
    }

    @Test
    @DisplayName("Should retrieve biomarker longitudinal history and computed trend trajectory")
    void testGetBiomarkerHistoryWithTrend() throws Exception {
        // Create dummy report
        Report report = new Report();
        report.setUser(testUser);
        report.setOriginalFilename("test_report.png");
        report.setStoragePath("/dummy/path.png");
        report.setMimeType("image/png");
        report.setFileSizeBytes(1024L);
        report.setFileHashSha256("dummyhash" + UUID.randomUUID().toString().replace("-", ""));
        report.setStatus(ReportStatus.COMPLETED);
        report = reportRepository.saveAndFlush(report);

        // Add 2 measurements for Fasting Blood Glucose (80 mg/dL -> 100 mg/dL = +25% RISING)
        Measurement m1 = new Measurement();
        m1.setReport(report);
        m1.setUser(testUser);
        m1.setBiomarker(glucoseBiomarker);
        m1.setExtractedName("FBS");
        m1.setObservedValueRaw("80.0");
        m1.setObservedValueNumeric(BigDecimal.valueOf(80.0));
        m1.setExtractedUnit("mg/dL");
        m1.setNormalizedValueNumeric(BigDecimal.valueOf(80.0));
        m1.setNormalizedUnit("mg/dL");
        m1.setStatus(MeasurementStatus.NORMAL);
        m1.setConfidence(BigDecimal.valueOf(0.99));
        m1.setPageNumber(1);
        m1.setSourceTextSnippet("FBS 80 mg/dL");
        measurementRepository.saveAndFlush(m1);

        Measurement m2 = new Measurement();
        m2.setReport(report);
        m2.setUser(testUser);
        m2.setBiomarker(glucoseBiomarker);
        m2.setExtractedName("FBS");
        m2.setObservedValueRaw("100.0");
        m2.setObservedValueNumeric(BigDecimal.valueOf(100.0));
        m2.setExtractedUnit("mg/dL");
        m2.setNormalizedValueNumeric(BigDecimal.valueOf(100.0));
        m2.setNormalizedUnit("mg/dL");
        m2.setStatus(MeasurementStatus.HIGH);
        m2.setConfidence(BigDecimal.valueOf(0.99));
        m2.setPageNumber(1);
        m2.setSourceTextSnippet("FBS 100 mg/dL");
        measurementRepository.saveAndFlush(m2);

        // Fetch history
        mockMvc.perform(get("/api/v1/biomarkers/" + glucoseBiomarker.getId() + "/history")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canonicalName", is("Fasting Blood Glucose")))
                .andExpect(jsonPath("$.statistics.readingsCount", is(2)))
                .andExpect(jsonPath("$.statistics.latestValue", is(100.0)))
                .andExpect(jsonPath("$.statistics.previousValue", is(80.0)))
                .andExpect(jsonPath("$.statistics.deltaPercentage", is(25.0)))
                .andExpect(jsonPath("$.statistics.trajectory", is("RISING")))
                .andExpect(jsonPath("$.dataPoints", hasSize(2)));
    }

    @Test
    @DisplayName("Should retrieve patient dashboard summary")
    void testGetDashboardSummary() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReports", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.totalMeasurements", is(greaterThanOrEqualTo(0))))
                .andExpect(jsonPath("$.abnormalCount", is(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("Should reject unauthenticated dashboard request with 401")
    void testUnauthenticatedDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isUnauthorized());
    }
}
