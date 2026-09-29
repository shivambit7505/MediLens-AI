package com.medilens.dto.report;

import com.medilens.model.Report;
import com.medilens.model.ReportStatus;

import java.time.Instant;
import java.util.UUID;

public record ReportResponseDto(
        UUID id,
        String originalFilename,
        String mimeType,
        Long fileSizeBytes,
        ReportStatus status,
        Integer pageCount,
        int measurementCount,
        Instant processingStartedAt,
        Instant processingCompletedAt,
        String failureReason,
        Instant createdAt
) {
    public static ReportResponseDto fromEntity(Report report, int measurementCount) {
        return new ReportResponseDto(
                report.getId(),
                report.getOriginalFilename(),
                report.getMimeType(),
                report.getFileSizeBytes(),
                report.getStatus(),
                report.getPageCount(),
                measurementCount,
                report.getProcessingStartedAt(),
                report.getProcessingCompletedAt(),
                report.getFailureReason(),
                report.getCreatedAt()
        );
    }
}
