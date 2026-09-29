package com.medilens.dto.trend;

import com.medilens.dto.report.MeasurementResponseDto;
import com.medilens.dto.report.ReportResponseDto;

import java.util.List;

public record PatientDashboardSummaryDto(
        long totalReports,
        long totalMeasurements,
        long abnormalCount,
        long criticalCount,
        List<ReportResponseDto> recentReports,
        List<MeasurementResponseDto> recentAbnormalMeasurements
) {}
