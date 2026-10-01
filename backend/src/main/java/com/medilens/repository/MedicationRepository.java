package com.medilens.repository;

import com.medilens.model.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    Optional<Medication> findByGenericNameIgnoreCase(String genericName);

    @Query("SELECT m FROM Medication m WHERE LOWER(m.brandName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Medication> searchByName(@Param("query") String query);
}
