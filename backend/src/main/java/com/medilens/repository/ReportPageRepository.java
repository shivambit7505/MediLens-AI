package com.medilens.repository;

import com.medilens.model.ReportPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportPageRepository extends JpaRepository<ReportPage, UUID> {
    List<ReportPage> findByReportIdOrderByPageNumberAsc(UUID reportId);
}
