package com.medilens.repository;

import com.medilens.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {
    List<Measurement> findByReportIdAndUserId(UUID reportId, UUID userId);
    List<Measurement> findByUserIdAndBiomarkerIdOrderByMeasurementDateAsc(UUID userId, UUID biomarkerId);
}
