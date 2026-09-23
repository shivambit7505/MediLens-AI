package com.medilens.repository;

import com.medilens.model.ReferenceRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReferenceRangeRepository extends JpaRepository<ReferenceRange, UUID> {
    List<ReferenceRange> findByBiomarkerId(UUID biomarkerId);
}
