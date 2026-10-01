package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medications")
public class Medication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "generic_name", nullable = false)
    private String genericName;

    @Column(name = "rxnorm_cui", length = 50)
    private String rxnormCui;

    @Column(name = "therapeutic_class", nullable = false)
    private String therapeuticClass;

    @Column(name = "standard_dosage_guidelines", columnDefinition = "TEXT")
    private String standardDosageGuidelines;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Medication() {}

    public Medication(UUID id, String brandName, String genericName, String rxnormCui,
                      String therapeuticClass, String standardDosageGuidelines, Instant createdAt) {
        this.id = id;
        this.brandName = brandName;
        this.genericName = genericName;
        this.rxnormCui = rxnormCui;
        this.therapeuticClass = therapeuticClass;
        this.standardDosageGuidelines = standardDosageGuidelines;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getRxnormCui() {
        return rxnormCui;
    }

    public void setRxnormCui(String rxnormCui) {
        this.rxnormCui = rxnormCui;
    }

    public String getTherapeuticClass() {
        return therapeuticClass;
    }

    public void setTherapeuticClass(String therapeuticClass) {
        this.therapeuticClass = therapeuticClass;
    }

    public String getStandardDosageGuidelines() {
        return standardDosageGuidelines;
    }

    public void setStandardDosageGuidelines(String standardDosageGuidelines) {
        this.standardDosageGuidelines = standardDosageGuidelines;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
