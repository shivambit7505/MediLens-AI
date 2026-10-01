package com.medilens.service;

import com.medilens.model.*;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdfExportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private MeasurementRepository measurementRepository;

    @InjectMocks
    private PdfExportService pdfExportService;

    private User user;
    private Report report;
    private Biomarker biomarker;
    private Measurement measurement;

    @BeforeEach
    void setUp() {
        user = new User(UUID.randomUUID(), "john.doe@medilens.ai", "hash", "John", "Doe",
                LocalDate.of(1985, 3, 15), "MALE", Role.ROLE_PATIENT, true, Instant.now(), Instant.now());

        report = new Report();
        report.setId(UUID.randomUUID());
        report.setUser(user);
        report.setOriginalFilename("metabolic_panel.pdf");
        report.setStoragePath("uploads/metabolic_panel.pdf");
        report.setMimeType("application/pdf");
        report.setFileSizeBytes(1024L);
        report.setFileHashSha256("hash123");
        report.setStatus(ReportStatus.COMPLETED);
        report.setPageCount(1);
        report.setCreatedAt(Instant.now());

        biomarker = new Biomarker(UUID.randomUUID(), "Fasting Blood Glucose", "1558-6", "36048009",
                "Metabolic", "mg/dL", "Blood sugar", "Clinical significance", "[]", Instant.now(), Instant.now());

        measurement = new Measurement();
        measurement.setId(UUID.randomUUID());
        measurement.setReport(report);
        measurement.setUser(user);
        measurement.setBiomarker(biomarker);
        measurement.setExtractedName("Glucose");
        measurement.setObservedValueRaw("126.0");
        measurement.setNormalizedValueNumeric(new BigDecimal("126.0"));
        measurement.setNormalizedUnit("mg/dL");
        measurement.setExtractedReferenceText("70-99 mg/dL");
        measurement.setStatus(MeasurementStatus.HIGH);
        measurement.setConfidence(new BigDecimal("0.98"));
        measurement.setPageNumber(1);
        measurement.setSourceTextSnippet("Glucose: 126.0 mg/dL");
    }

    @Test
    @DisplayName("Should generate valid PDF byte array starting with %PDF magic bytes")
    void testGenerateReportPdf() {
        when(reportRepository.findById(report.getId())).thenReturn(Optional.of(report));
        when(measurementRepository.findByReportIdAndUserId(report.getId(), user.getId()))
                .thenReturn(List.of(measurement));

        byte[] pdfBytes = pdfExportService.generateReportPdf(user.getId(), report.getId());

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);

        // Verify PDF Magic Bytes (%PDF-)
        String header = new String(pdfBytes, 0, 5);
        assertThat(header).isEqualTo("%PDF-");
    }
}
