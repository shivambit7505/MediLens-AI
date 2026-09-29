package com.medilens.service;

import com.medilens.dto.report.MeasurementResponseDto;
import com.medilens.dto.report.ReportResponseDto;
import com.medilens.dto.trend.*;
import com.medilens.exception.ResourceNotFoundException;
import com.medilens.model.Biomarker;
import com.medilens.model.Measurement;
import com.medilens.model.MeasurementStatus;
import com.medilens.model.Report;
import com.medilens.repository.BiomarkerRepository;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class BiomarkerTrendService {

    private final BiomarkerRepository biomarkerRepository;
    private final MeasurementRepository measurementRepository;
    private final ReportRepository reportRepository;

    public BiomarkerTrendService(
            BiomarkerRepository biomarkerRepository,
            MeasurementRepository measurementRepository,
            ReportRepository reportRepository
    ) {
        this.biomarkerRepository = biomarkerRepository;
        this.measurementRepository = measurementRepository;
        this.reportRepository = reportRepository;
    }

    public List<BiomarkerSummaryDto> getAllBiomarkers() {
        return biomarkerRepository.findAll().stream()
                .map(BiomarkerSummaryDto::fromEntity)
                .toList();
    }

    public BiomarkerHistoryResponseDto getBiomarkerHistory(UUID biomarkerId, UUID userId) {
        Biomarker biomarker = biomarkerRepository.findById(biomarkerId)
                .orElseThrow(() -> new ResourceNotFoundException("Biomarker", "id", biomarkerId));

        List<Measurement> measurements = measurementRepository
                .findByUserIdAndBiomarkerIdOrderByCreatedAtAsc(userId, biomarkerId);

        List<BiomarkerDataPointDto> dataPoints = measurements.stream()
                .map(BiomarkerDataPointDto::fromEntity)
                .toList();

        TrendStatisticsDto statistics = calculateStatistics(dataPoints);

        return new BiomarkerHistoryResponseDto(
                biomarker.getId(),
                biomarker.getCanonicalName(),
                biomarker.getCodeLoinc(),
                biomarker.getStandardUnit(),
                biomarker.getCategory(),
                statistics,
                dataPoints
        );
    }

    public TrendStatisticsDto calculateStatistics(List<BiomarkerDataPointDto> dataPoints) {
        if (dataPoints == null || dataPoints.isEmpty()) {
            return new TrendStatisticsDto(null, null, null, null, null, null, "INSUFFICIENT_DATA", 0);
        }

        int count = dataPoints.size();
        BigDecimal latest = dataPoints.get(count - 1).normalizedValue();

        BigDecimal min = dataPoints.stream()
                .map(BiomarkerDataPointDto::normalizedValue)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(latest);

        BigDecimal max = dataPoints.stream()
                .map(BiomarkerDataPointDto::normalizedValue)
                .filter(Objects::nonNull)
                .max(BigDecimal::compareTo)
                .orElse(latest);

        if (count == 1) {
            return new TrendStatisticsDto(latest, latest, min, max, BigDecimal.ZERO, 0.0, "INSUFFICIENT_DATA", 1);
        }

        BigDecimal previous = dataPoints.get(count - 2).normalizedValue();
        BigDecimal delta = latest.subtract(previous);

        Double deltaPct = null;
        String trajectory;

        if (previous.compareTo(BigDecimal.ZERO) != 0) {
            deltaPct = delta.divide(previous, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            if (deltaPct > 5.0) {
                trajectory = "RISING";
            } else if (deltaPct < -5.0) {
                trajectory = "FALLING";
            } else {
                trajectory = "STABLE";
            }
        } else {
            trajectory = "STABLE";
        }

        return new TrendStatisticsDto(latest, previous, min, max, delta, deltaPct, trajectory, count);
    }

    public PatientDashboardSummaryDto getDashboardSummary(UUID userId) {
        long totalReports = reportRepository.countByUserId(userId);
        long totalMeasurements = measurementRepository.findByUserIdOrderByCreatedAtDesc(userId).size();
        long criticalCount = measurementRepository.countByUserIdAndStatus(userId, MeasurementStatus.CRITICAL);
        long abnormalCount = measurementRepository.countByUserIdAndStatusIn(
                userId, List.of(MeasurementStatus.LOW, MeasurementStatus.HIGH, MeasurementStatus.CRITICAL)
        );

        List<Report> topReports = reportRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        List<ReportResponseDto> recentReports = topReports.stream().map(r -> {
            int mCount = measurementRepository.findByReportIdAndUserId(r.getId(), userId).size();
            return ReportResponseDto.fromEntity(r, mCount);
        }).toList();

        List<Measurement> abnormalMeasurements = measurementRepository
                .findByUserIdAndStatusInOrderByCreatedAtDesc(
                        userId, List.of(MeasurementStatus.LOW, MeasurementStatus.HIGH, MeasurementStatus.CRITICAL)
                );

        List<MeasurementResponseDto> recentAbnormal = abnormalMeasurements.stream()
                .limit(10)
                .map(MeasurementResponseDto::fromEntity)
                .toList();

        return new PatientDashboardSummaryDto(
                totalReports,
                totalMeasurements,
                abnormalCount,
                criticalCount,
                recentReports,
                recentAbnormal
        );
    }
}
