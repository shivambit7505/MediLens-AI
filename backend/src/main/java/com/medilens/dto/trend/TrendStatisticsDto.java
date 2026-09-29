package com.medilens.dto.trend;

import java.math.BigDecimal;

public record TrendStatisticsDto(
        BigDecimal latestValue,
        BigDecimal previousValue,
        BigDecimal minValue,
        BigDecimal maxValue,
        BigDecimal deltaValue,
        Double deltaPercentage,
        String trajectory, // RISING, FALLING, STABLE, INSUFFICIENT_DATA
        int readingsCount
) {}
