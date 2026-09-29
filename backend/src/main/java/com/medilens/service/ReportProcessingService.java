package com.medilens.service;

import com.medilens.client.AiExtractionResponse;
import com.medilens.client.AiMeasurementDto;
import com.medilens.client.AiOcrResponse;
import com.medilens.client.AiRagRequest;
import com.medilens.client.AiRagResponse;
import com.medilens.client.AiServiceClient;
import com.medilens.document.DocumentProcessor;
import com.medilens.dto.report.MeasurementResponseDto;
import com.medilens.dto.report.ReportDetailResponseDto;
import com.medilens.dto.report.ReportExplanationResponseDto;
import com.medilens.dto.report.ReportResponseDto;
import com.medilens.exception.ResourceNotFoundException;
import com.medilens.model.*;
import com.medilens.repository.*;
import com.medilens.storage.FileValidator;
import com.medilens.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ReportProcessingService.class);

    private final ReportRepository reportRepository;
    private final ReportPageRepository reportPageRepository;
    private final BiomarkerRepository biomarkerRepository;
    private final MeasurementRepository measurementRepository;
    private final StorageService storageService;
    private final FileValidator fileValidator;
    private final DocumentProcessor documentProcessor;
    private final AiServiceClient aiServiceClient;
    private final AuditService auditService;

    public ReportProcessingService(
            ReportRepository reportRepository,
            ReportPageRepository reportPageRepository,
            BiomarkerRepository biomarkerRepository,
            MeasurementRepository measurementRepository,
            StorageService storageService,
            FileValidator fileValidator,
            DocumentProcessor documentProcessor,
            AiServiceClient aiServiceClient,
            AuditService auditService
    ) {
        this.reportRepository = reportRepository;
        this.reportPageRepository = reportPageRepository;
        this.biomarkerRepository = biomarkerRepository;
        this.measurementRepository = measurementRepository;
        this.storageService = storageService;
        this.fileValidator = fileValidator;
        this.documentProcessor = documentProcessor;
        this.aiServiceClient = aiServiceClient;
        this.auditService = auditService;
    }

    @Transactional
    public ReportResponseDto uploadReport(User user, MultipartFile file, String ipAddress, String userAgent) {
        FileValidator.ValidatedFileInfo fileInfo = fileValidator.validate(file);
        String sha256 = storageService.computeSha256(file);

        // Deduplication check: return existing report if previously uploaded and processed
        Optional<Report> existingReportOpt = reportRepository.findByUserIdAndFileHashSha256(user.getId(), sha256);
        if (existingReportOpt.isPresent()) {
            Report existing = existingReportOpt.get();
            log.info("Deduplication: Identical report already exists for user {} with SHA-256 {}", user.getId(), sha256);
            auditService.logEvent(user.getId(), "REPORT_DUPLICATE_DETECTED", "REPORT", existing.getId().toString(),
                    ipAddress, userAgent, 200, "{\"existingReportId\":\"" + existing.getId() + "\"}");
            int count = measurementRepository.findByReportIdAndUserId(existing.getId(), user.getId()).size();
            return ReportResponseDto.fromEntity(existing, count);
        }

        UUID reportId = UUID.randomUUID();
        String storedPath = storageService.storeReport(user.getId(), reportId, file, fileInfo.sanitizedFilename());

        Report report = new Report();
        report.setId(reportId);
        report.setUser(user);
        report.setOriginalFilename(fileInfo.sanitizedFilename());
        report.setStoragePath(storedPath);
        report.setMimeType(fileInfo.detectedMimeType());
        report.setFileSizeBytes(file.getSize());
        report.setFileHashSha256(sha256);
        report.setStatus(ReportStatus.UPLOADED);
        report.setPageCount(0);
        report = reportRepository.save(report);

        auditService.logEvent(user.getId(), "REPORT_UPLOADED", "REPORT", report.getId().toString(),
                ipAddress, userAgent, 201, "{\"filename\":\"" + fileInfo.sanitizedFilename() + "\"}");

        // Process report synchronously through pipeline
        processReport(report, user);

        int measurementCount = measurementRepository.findByReportIdAndUserId(report.getId(), user.getId()).size();
        return ReportResponseDto.fromEntity(report, measurementCount);
    }

    public void processReport(Report report, User user) {
        try {
            report.setStatus(ReportStatus.PREPROCESSING);
            report.setProcessingStartedAt(Instant.now());
            reportRepository.save(report);

            Path filePath = storageService.loadPath(report.getStoragePath());
            List<DocumentProcessor.RenderedPage> renderedPages =
                    documentProcessor.processDocumentToPages(filePath, report.getMimeType());

            report.setPageCount(renderedPages.size());
            report.setStatus(ReportStatus.OCR_PROCESSING);
            reportRepository.save(report);

            StringBuilder combinedOcrText = new StringBuilder();

            for (DocumentProcessor.RenderedPage page : renderedPages) {
                String pageImagePath = storageService.storePageImage(
                        user.getId(), report.getId(), page.pageNumber(), page.imageBytes()
                );

                AiOcrResponse ocrResponse = aiServiceClient.processOcrPage(pageImagePath, page.pageNumber());

                ReportPage reportPage = ReportPage.builder()
                        .report(report)
                        .pageNumber(page.pageNumber())
                        .imageStoragePath(pageImagePath)
                        .ocrRawText(ocrResponse.rawText())
                        .ocrConfidenceScore(BigDecimal.valueOf(ocrResponse.confidenceScore()))
                        .ocrEngineUsed(ocrResponse.engineUsed())
                        .build();

                reportPageRepository.save(reportPage);
                combinedOcrText.append(ocrResponse.rawText()).append("\n");
            }

            // Extract and validate biomarkers via AI Service
            report.setStatus(ReportStatus.EXTRACTING);
            reportRepository.save(report);

            AiExtractionResponse extractionResponse = aiServiceClient.extractAndValidateBiomarkers(
                    combinedOcrText.toString(), 30.0, "ALL"
            );

            report.setStatus(ReportStatus.NORMALIZING);
            reportRepository.save(report);

            for (AiMeasurementDto dto : extractionResponse.measurements()) {
                Biomarker biomarker = biomarkerRepository.findByCanonicalName(dto.canonicalName())
                        .orElseGet(() -> {
                            Biomarker b = new Biomarker();
                            b.setCanonicalName(dto.canonicalName());
                            b.setCodeLoinc(dto.codeLoinc());
                            b.setCategory("Laboratory");
                            b.setStandardUnit(dto.normalizedUnit());
                            return biomarkerRepository.save(b);
                        });

                MeasurementStatus mStatus;
                try {
                    mStatus = MeasurementStatus.valueOf(dto.status().toUpperCase());
                } catch (Exception ex) {
                    mStatus = MeasurementStatus.UNKNOWN;
                }

                Measurement measurement = new Measurement();
                measurement.setReport(report);
                measurement.setUser(user);
                measurement.setBiomarker(biomarker);
                measurement.setExtractedName(dto.extractedName());
                measurement.setObservedValueRaw(dto.observedValueRaw());
                measurement.setObservedValueNumeric(dto.observedValueNumeric());
                measurement.setExtractedUnit(dto.extractedUnit());
                measurement.setNormalizedValueNumeric(dto.normalizedValueNumeric());
                measurement.setNormalizedUnit(dto.normalizedUnit());
                measurement.setExtractedReferenceText(dto.referenceIntervalRaw());
                measurement.setStatus(mStatus);
                measurement.setConfidence(dto.confidence() != null ? dto.confidence() : BigDecimal.valueOf(0.95));
                measurement.setPageNumber(1);
                measurement.setSourceTextSnippet(dto.sourceSnippet() != null ? dto.sourceSnippet() : dto.extractedName());

                measurementRepository.save(measurement);
            }

            report.setStatus(ReportStatus.COMPLETED);
            report.setProcessingCompletedAt(Instant.now());
            reportRepository.save(report);

            auditService.logEvent(user.getId(), "REPORT_PROCESSED_SUCCESS", "REPORT", report.getId().toString(),
                    "127.0.0.1", "MediLensEngine", 200,
                    "{\"pages\":" + renderedPages.size() + ",\"measurements\":" + extractionResponse.extractedCount() + "}");

        } catch (Exception ex) {
            log.error("Report processing pipeline failed for report {}: {}", report.getId(), ex.getMessage(), ex);
            report.setStatus(ReportStatus.FAILED);
            report.setFailureReason(ex.getMessage());
            report.setProcessingCompletedAt(Instant.now());
            reportRepository.save(report);

            auditService.logEvent(user.getId(), "REPORT_PROCESSED_FAILED", "REPORT", report.getId().toString(),
                    "127.0.0.1", "MediLensEngine", 500, "{\"error\":\"" + ex.getMessage() + "\"}");
        }
    }

    public Page<ReportResponseDto> getUserReports(UUID userId, Pageable pageable) {
        Page<Report> reports = reportRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return reports.map(r -> {
            int count = measurementRepository.findByReportIdAndUserId(r.getId(), userId).size();
            return ReportResponseDto.fromEntity(r, count);
        });
    }

    public ReportDetailResponseDto getReportDetail(UUID reportId, UUID userId) {
        Report report = reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        List<ReportPage> pages = reportPageRepository.findByReportIdOrderByPageNumberAsc(reportId);
        List<Measurement> measurements = measurementRepository.findByReportIdAndUserId(reportId, userId);

        List<MeasurementResponseDto> measurementDtos = measurements.stream()
                .map(MeasurementResponseDto::fromEntity)
                .toList();

        return ReportDetailResponseDto.fromEntities(report, pages, measurementDtos);
    }

    public List<MeasurementResponseDto> getReportMeasurements(UUID reportId, UUID userId) {
        if (!reportRepository.existsByIdAndUserId(reportId, userId)) {
            throw new ResourceNotFoundException("Report", "id", reportId);
        }
        return measurementRepository.findByReportIdAndUserId(reportId, userId).stream()
                .map(MeasurementResponseDto::fromEntity)
                .toList();
    }

    public Resource getPageImageResource(UUID reportId, int pageNumber, UUID userId) {
        Report report = reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        List<ReportPage> pages = reportPageRepository.findByReportIdOrderByPageNumberAsc(reportId);
        ReportPage targetPage = pages.stream()
                .filter(p -> p.getPageNumber() == pageNumber)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ReportPage", "pageNumber", pageNumber));

        return storageService.loadAsResource(targetPage.getImageStoragePath());
    }

    @Transactional(readOnly = true)
    public ReportExplanationResponseDto getReportExplanation(UUID reportId, User user) {
        Report report = reportRepository.findByIdAndUserId(reportId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        List<Measurement> measurements = measurementRepository.findByReportIdAndUserId(reportId, user.getId());

        double patientAgeYears = 35.0;
        if (user.getDateOfBirth() != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(user.getDateOfBirth(), java.time.LocalDate.now());
            patientAgeYears = days / 365.25;
        }
        String patientGender = user.getGender() != null ? user.getGender() : "ALL";

        List<AiRagRequest.AiRagBiomarkerInput> inputs = measurements.stream()
                .map(m -> new AiRagRequest.AiRagBiomarkerInput(
                        m.getExtractedName(),
                        m.getBiomarker() != null ? m.getBiomarker().getCanonicalName() : m.getExtractedName(),
                        m.getObservedValueRaw(),
                        m.getNormalizedValueNumeric() != null ? m.getNormalizedValueNumeric().doubleValue() : 0.0,
                        m.getNormalizedUnit() != null ? m.getNormalizedUnit() : "",
                        m.getExtractedReferenceText(),
                        m.getStatus() != null ? m.getStatus().name() : "UNKNOWN"
                ))
                .toList();

        AiRagRequest ragRequest = new AiRagRequest(
                report.getId().toString(),
                patientAgeYears,
                patientGender,
                inputs
        );

        AiRagResponse aiResponse = aiServiceClient.generateReportExplanation(ragRequest);

        List<ReportExplanationResponseDto.BiomarkerExplanationDto> findings = aiResponse.findings() != null
                ? aiResponse.findings().stream()
                .map(f -> new ReportExplanationResponseDto.BiomarkerExplanationDto(
                        f.canonicalName(),
                        f.observedValue(),
                        f.status(),
                        f.referenceInterval(),
                        f.explanation(),
                        f.clinicalSignificance(),
                        f.lifestyleGuidance(),
                        f.sources()
                ))
                .toList()
                : List.of();

        List<ReportExplanationResponseDto.EvidenceSourceDto> sources = aiResponse.citedSources() != null
                ? aiResponse.citedSources().stream()
                .map(s -> new ReportExplanationResponseDto.EvidenceSourceDto(
                        s.chunkId(),
                        s.title(),
                        s.source(),
                        s.category()
                ))
                .toList()
                : List.of();

        return new ReportExplanationResponseDto(
                report.getId(),
                aiResponse.summary(),
                findings,
                aiResponse.questionsForDoctor(),
                aiResponse.criticalAlert(),
                aiResponse.disclaimer(),
                sources,
                aiResponse.safetyAuditPassed()
        );
    }
}
