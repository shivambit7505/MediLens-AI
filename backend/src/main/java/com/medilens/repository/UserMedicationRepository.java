package com.medilens.repository;

import com.medilens.model.UserMedication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserMedicationRepository extends JpaRepository<UserMedication, UUID> {
    List<UserMedication> findByUserIdAndIsActiveTrue(UUID userId);
    List<UserMedication> findByUserId(UUID userId);
}
