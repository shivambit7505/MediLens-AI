package com.medilens.repository;

import com.medilens.model.Measurement;
import com.medilens.model.MeasurementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {
    List<Measurement> findByReportIdAndUserId(UUID reportId, UUID userId);
    List<Measurement> findByUserIdAndBiomarkerIdOrderByMeasurementDateAsc(UUID userId, UUID biomarkerId);
    List<Measurement> findByUserIdAndBiomarkerIdOrderByCreatedAtAsc(UUID userId, UUID biomarkerId);
    List<Measurement> findByUserIdAndStatusInOrderByCreatedAtDesc(UUID userId, Collection<MeasurementStatus> statuses);
    List<Measurement> findByUserIdOrderByCreatedAtDesc(UUID userId);
    long countByUserIdAndStatus(UUID userId, MeasurementStatus status);
    long countByUserIdAndStatusIn(UUID userId, Collection<MeasurementStatus> statuses);
}
