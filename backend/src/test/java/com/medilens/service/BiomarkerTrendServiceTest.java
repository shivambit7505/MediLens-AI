package com.medilens.service;

import com.medilens.dto.trend.BiomarkerDataPointDto;
import com.medilens.dto.trend.TrendStatisticsDto;
import com.medilens.model.MeasurementStatus;
import com.medilens.repository.BiomarkerRepository;
import com.medilens.repository.MeasurementRepository;
import com.medilens.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BiomarkerTrendServiceTest {

    private BiomarkerTrendService trendService;

    @BeforeEach
    void setUp() {
        BiomarkerRepository biomarkerRepo = Mockito.mock(BiomarkerRepository.class);
        MeasurementRepository measurementRepo = Mockito.mock(MeasurementRepository.class);
        ReportRepository reportRepo = Mockito.mock(ReportRepository.class);

        trendService = new BiomarkerTrendService(biomarkerRepo, measurementRepo, reportRepo);
    }

    private BiomarkerDataPointDto createPoint(BigDecimal value, LocalDate date) {
        return new BiomarkerDataPointDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                date,
                Instant.now(),
                value.toString(),
                value,
                "mg/dL",
                MeasurementStatus.NORMAL,
                "70-99"
        );
    }

    @Test
    @DisplayName("Should return INSUFFICIENT_DATA when no data points are present")
    void testEmptyDataPoints() {
        TrendStatisticsDto stats = trendService.calculateStatistics(List.of());
        assertEquals("INSUFFICIENT_DATA", stats.trajectory());
        assertEquals(0, stats.readingsCount());
        assertNull(stats.latestValue());
    }

    @Test
    @DisplayName("Should return INSUFFICIENT_DATA with count 1 for a single reading")
    void testSingleDataPoint() {
        List<BiomarkerDataPointDto> points = List.of(
                createPoint(BigDecimal.valueOf(95.0), LocalDate.of(2025, 1, 1))
        );
        TrendStatisticsDto stats = trendService.calculateStatistics(points);
        assertEquals("INSUFFICIENT_DATA", stats.trajectory());
        assertEquals(1, stats.readingsCount());
        assertEquals(BigDecimal.valueOf(95.0), stats.latestValue());
        assertEquals(BigDecimal.valueOf(95.0), stats.minValue());
        assertEquals(BigDecimal.valueOf(95.0), stats.maxValue());
    }

    @Test
    @DisplayName("Should detect RISING trajectory when change exceeds +5%")
    void testRisingTrajectory() {
        List<BiomarkerDataPointDto> points = List.of(
                createPoint(BigDecimal.valueOf(80.0), LocalDate.of(2025, 1, 1)),
                createPoint(BigDecimal.valueOf(100.0), LocalDate.of(2025, 3, 1))
        );
        // Change: (100 - 80) / 80 = +25%
        TrendStatisticsDto stats = trendService.calculateStatistics(points);
        assertEquals("RISING", stats.trajectory());
        assertEquals(2, stats.readingsCount());
        assertEquals(BigDecimal.valueOf(100.0), stats.latestValue());
        assertEquals(BigDecimal.valueOf(80.0), stats.previousValue());
        assertEquals(BigDecimal.valueOf(20.0), stats.deltaValue());
        assertEquals(25.0, stats.deltaPercentage());
    }

    @Test
    @DisplayName("Should detect FALLING trajectory when change is below -5%")
    void testFallingTrajectory() {
        List<BiomarkerDataPointDto> points = List.of(
                createPoint(BigDecimal.valueOf(100.0), LocalDate.of(2025, 1, 1)),
                createPoint(BigDecimal.valueOf(85.0), LocalDate.of(2025, 3, 1))
        );
        // Change: (85 - 100) / 100 = -15%
        TrendStatisticsDto stats = trendService.calculateStatistics(points);
        assertEquals("FALLING", stats.trajectory());
        assertEquals(-15.0, stats.deltaPercentage());
    }

    @Test
    @DisplayName("Should detect STABLE trajectory when change is within +/-5%")
    void testStableTrajectory() {
        List<BiomarkerDataPointDto> points = List.of(
                createPoint(BigDecimal.valueOf(100.0), LocalDate.of(2025, 1, 1)),
                createPoint(BigDecimal.valueOf(102.0), LocalDate.of(2025, 3, 1))
        );
        // Change: (102 - 100) / 100 = +2% (within +/-5%)
        TrendStatisticsDto stats = trendService.calculateStatistics(points);
        assertEquals("STABLE", stats.trajectory());
        assertEquals(2.0, stats.deltaPercentage());
    }

    @Test
    @DisplayName("Should calculate accurate min and max across multiple readings")
    void testMinMaxMultiReadings() {
        List<BiomarkerDataPointDto> points = List.of(
                createPoint(BigDecimal.valueOf(110.0), LocalDate.of(2025, 1, 1)),
                createPoint(BigDecimal.valueOf(85.0), LocalDate.of(2025, 2, 1)),
                createPoint(BigDecimal.valueOf(130.0), LocalDate.of(2025, 3, 1)),
                createPoint(BigDecimal.valueOf(95.0), LocalDate.of(2025, 4, 1))
        );
        TrendStatisticsDto stats = trendService.calculateStatistics(points);
        assertEquals(4, stats.readingsCount());
        assertEquals(BigDecimal.valueOf(95.0), stats.latestValue());
        assertEquals(BigDecimal.valueOf(130.0), stats.previousValue());
        assertEquals(BigDecimal.valueOf(85.0), stats.minValue());
        assertEquals(BigDecimal.valueOf(130.0), stats.maxValue());
    }
}
