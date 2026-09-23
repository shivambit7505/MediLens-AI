package com.medilens.repository;

import com.medilens.model.Biomarker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BiomarkerRepository extends JpaRepository<Biomarker, UUID> {
    Optional<Biomarker> findByCanonicalName(String canonicalName);
    Optional<Biomarker> findByCodeLoinc(String codeLoinc);
}
