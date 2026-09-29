package com.medilens.repository;

import com.medilens.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<Report> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    Optional<Report> findByUserIdAndFileHashSha256(UUID userId, String fileHashSha256);
}
