package com.medilens.repository;

import com.medilens.model.TriageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TriageRuleRepository extends JpaRepository<TriageRule, UUID> {
    List<TriageRule> findByIsActiveTrue();
    List<TriageRule> findByBiomarkerCanonicalNameIgnoreCaseAndIsActiveTrue(String biomarkerCanonicalName);
}
