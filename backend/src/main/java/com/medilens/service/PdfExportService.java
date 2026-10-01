package com.medilens.service;

import com.medilens.exception.ResourceNotFoundException;
import com.medilens.exception.UnauthorizedException;
import com.medilens.model.Measurement;
import com.medilens.model.Report;
import com.medilens.model.User;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class PdfExportService {

    private static final Logger log = LoggerFactory.getLogger(PdfExportService.class);

    private final ReportRepository reportRepository;
    private final MeasurementRepository measurementRepository;

    public PdfExportService(ReportRepository reportRepository, MeasurementRepository measurementRepository) {
        this.reportRepository = reportRepository;
        this.measurementRepository = measurementRepository;
    }

    public byte[] generateReportPdf(UUID userId, UUID reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        if (!report.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Unauthorized to access this report");
        }

        User user = report.getUser();
        List<Measurement> measurements = measurementRepository.findByReportIdAndUserId(reportId, userId);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

            try (PDPageContentStream cs = new PDPageContentStream(document, page)) {
                float y = 790;
                float margin = 50;

                // --- HEADER ---
                cs.beginText();
                cs.setFont(fontBold, 18);
                cs.newLineAtOffset(margin, y);
                cs.showText("MEDILENS AI");
                cs.endText();

                cs.beginText();
                cs.setFont(fontRegular, 10);
                cs.newLineAtOffset(margin + 130, y + 2);
                cs.showText("Clinical Laboratory Analysis & Healthcare Intelligence");
                cs.endText();

                y -= 15;
                cs.setLineWidth(1.5f);
                cs.moveTo(margin, y);
                cs.lineTo(545, y);
                cs.stroke();

                // --- REPORT & PATIENT METADATA ---
                y -= 25;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("PATIENT DEMOGRAPHICS & RECORD");
                cs.endText();

                y -= 16;
                cs.beginText();
                cs.setFont(fontRegular, 9);
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("Patient Name: %s %s       Gender: %s       DOB: %s",
                        user.getFirstName(), user.getLastName(),
                        user.getGender() != null ? user.getGender() : "N/A",
                        user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : "N/A"));
                cs.endText();

                y -= 14;
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));
                String createdStr = report.getCreatedAt() != null ? fmt.format(report.getCreatedAt()) : "N/A";
                cs.beginText();
                cs.setFont(fontRegular, 9);
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("Report File: %s       Processed: %s UTC",
                        report.getOriginalFilename(), createdStr));
                cs.endText();

                y -= 14;
                cs.beginText();
                cs.setFont(fontRegular, 9);
                cs.newLineAtOffset(margin, y);
                cs.showText(String.format("Report ID: %s       Status: %s",
                        report.getId(), report.getStatus()));
                cs.endText();

                y -= 20;
                cs.setLineWidth(0.5f);
                cs.moveTo(margin, y);
                cs.lineTo(545, y);
                cs.stroke();

                // --- MEASUREMENTS TABLE HEADER ---
                y -= 20;
                cs.beginText();
                cs.setFont(fontBold, 11);
                cs.newLineAtOffset(margin, y);
                cs.showText("VERIFIED CLINICAL BIOMARKERS");
                cs.endText();

                y -= 18;
                // Table header bar
                cs.beginText();
                cs.setFont(fontBold, 8);
                cs.newLineAtOffset(margin, y);
                cs.showText("BIOMARKER");
                cs.newLineAtOffset(160, 0);
                cs.showText("VALUE");
                cs.newLineAtOffset(60, 0);
                cs.showText("UNIT");
                cs.newLineAtOffset(80, 0);
                cs.showText("REF. INTERVAL");
                cs.newLineAtOffset(110, 0);
                cs.showText("STATUS");
                cs.endText();

                y -= 6;
                cs.setLineWidth(0.5f);
                cs.moveTo(margin, y);
                cs.lineTo(545, y);
                cs.stroke();

                // --- MEASUREMENTS ROWS ---
                y -= 14;
                if (measurements.isEmpty()) {
                    cs.beginText();
                    cs.setFont(fontOblique, 9);
                    cs.newLineAtOffset(margin, y);
                    cs.showText("No structured biomarkers extracted for this report.");
                    cs.endText();
                    y -= 15;
                } else {
                    for (Measurement m : measurements) {
                        if (y < 120) {
                            // Bottom of page threshold
                            break;
                        }

                        String bName = m.getBiomarker() != null ? m.getBiomarker().getCanonicalName() : m.getExtractedName();
                        if (bName.length() > 24) bName = bName.substring(0, 22) + "..";

                        Double val = m.getNormalizedValueNumeric() != null ? m.getNormalizedValueNumeric().doubleValue() :
                                (m.getObservedValueNumeric() != null ? m.getObservedValueNumeric().doubleValue() : null);
                        String valStr = val != null ? String.format("%.2f", val) : m.getObservedValueRaw();
                        String unitStr = m.getNormalizedUnit() != null ? m.getNormalizedUnit() :
                                (m.getExtractedUnit() != null ? m.getExtractedUnit() : "");
                        String refStr = m.getExtractedReferenceText() != null ? m.getExtractedReferenceText() : "See Notes";
                        if (refStr.length() > 18) refStr = refStr.substring(0, 16) + "..";
                        String statusStr = m.getStatus() != null ? m.getStatus().name() : "UNKNOWN";

                        cs.beginText();
                        cs.setFont(fontRegular, 8);
                        cs.newLineAtOffset(margin, y);
                        cs.showText(bName);
                        cs.newLineAtOffset(160, 0);
                        cs.showText(valStr);
                        cs.newLineAtOffset(60, 0);
                        cs.showText(unitStr);
                        cs.newLineAtOffset(80, 0);
                        cs.showText(refStr);
                        cs.newLineAtOffset(110, 0);

                        if ("CRITICAL".equalsIgnoreCase(statusStr) || "HIGH".equalsIgnoreCase(statusStr) || "LOW".equalsIgnoreCase(statusStr)) {
                            cs.setFont(fontBold, 8);
                        }
                        cs.showText(statusStr);
                        cs.endText();

                        y -= 14;
                    }
                }

                // --- FOOTER & STATUTORY DISCLAIMER ---
                y = 80;
                cs.setLineWidth(0.5f);
                cs.moveTo(margin, y);
                cs.lineTo(545, y);
                cs.stroke();

                y -= 14;
                cs.beginText();
                cs.setFont(fontBold, 7);
                cs.newLineAtOffset(margin, y);
                cs.showText("STATUTORY SAFETY DISCLAIMER & CLINICAL INTENT NOTICE:");
                cs.endText();

                y -= 10;
                cs.beginText();
                cs.setFont(fontOblique, 6.5f);
                cs.newLineAtOffset(margin, y);
                cs.showText("MediLens AI is an academic clinical data extraction platform. All biological ranges and status evaluations");
                cs.endText();

                y -= 9;
                cs.beginText();
                cs.setFont(fontOblique, 6.5f);
                cs.newLineAtOffset(margin, y);
                cs.showText("are deterministic. This document does not constitute a doctor's diagnosis, clinical order, or prescription.");
                cs.endText();

                y -= 9;
                cs.beginText();
                cs.setFont(fontRegular, 6.5f);
                cs.newLineAtOffset(margin, y);
                cs.showText("Always discuss abnormal or critical findings with your licensed healthcare provider immediately.");
                cs.endText();
            }

            document.save(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to generate report PDF for report ID {}", reportId, e);
            throw new RuntimeException("Failed to generate clinical PDF export", e);
        }
    }
}
