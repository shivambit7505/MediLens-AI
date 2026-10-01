package com.medilens.repository;

import com.medilens.model.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, UUID> {

    @Query("SELECT di FROM DrugInteraction di WHERE " +
           "(di.medicationA.id = :idA AND di.medicationB.id = :idB) OR " +
           "(di.medicationA.id = :idB AND di.medicationB.id = :idA)")
    Optional<DrugInteraction> findInteractionBetween(@Param("idA") UUID idA, @Param("idB") UUID idB);

    @Query("SELECT di FROM DrugInteraction di WHERE " +
           "di.medicationA.id IN :medIds AND di.medicationB.id IN :medIds")
    List<DrugInteraction> findAllInteractionsAmong(@Param("medIds") List<UUID> medIds);
}
