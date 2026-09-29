package com.medilens.dto.report;

import com.medilens.model.Report;
import com.medilens.model.ReportPage;
import com.medilens.model.ReportStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ReportDetailResponseDto(
        UUID id,
        String originalFilename,
        String mimeType,
        Long fileSizeBytes,
        ReportStatus status,
        Integer pageCount,
        Instant processingStartedAt,
        Instant processingCompletedAt,
        String failureReason,
        Instant createdAt,
        List<ReportPageSummaryDto> pages,
        List<MeasurementResponseDto> measurements
) {
    public record ReportPageSummaryDto(
            UUID id,
            int pageNumber,
            String ocrEngineUsed,
            double confidenceScore,
            int textLength
    ) {
        public static ReportPageSummaryDto fromEntity(ReportPage page) {
            double conf = page.getOcrConfidenceScore() != null ? page.getOcrConfidenceScore().doubleValue() : 0.0;
            int len = page.getOcrRawText() != null ? page.getOcrRawText().length() : 0;
            return new ReportPageSummaryDto(
                    page.getId(),
                    page.getPageNumber(),
                    page.getOcrEngineUsed(),
                    conf,
                    len
            );
        }
    }

    public static ReportDetailResponseDto fromEntities(
            Report report,
            List<ReportPage> pages,
            List<MeasurementResponseDto> measurements
    ) {
        List<ReportPageSummaryDto> pageDtos = pages.stream()
                .map(ReportPageSummaryDto::fromEntity)
                .toList();

        return new ReportDetailResponseDto(
                report.getId(),
                report.getOriginalFilename(),
                report.getMimeType(),
                report.getFileSizeBytes(),
                report.getStatus(),
                report.getPageCount(),
                report.getProcessingStartedAt(),
                report.getProcessingCompletedAt(),
                report.getFailureReason(),
                report.getCreatedAt(),
                pageDtos,
                measurements
        );
    }
}
