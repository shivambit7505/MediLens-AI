package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "biomarkers")
public class Biomarker {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "canonical_name", nullable = false, unique = true, length = 200)
    private String canonicalName;

    @Column(name = "code_loinc", length = 50)
    private String codeLoinc;

    @Column(name = "code_snomed", length = 50)
    private String codeSnomed;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "standard_unit", nullable = false, length = 50)
    private String standardUnit;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "clinical_significance", columnDefinition = "TEXT")
    private String clinicalSignificance;

    @Column(name = "aliases_json", columnDefinition = "TEXT")
    private String aliasesJson = "[]";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Biomarker() {}

    public Biomarker(UUID id, String canonicalName, String codeLoinc, String codeSnomed,
                     String category, String standardUnit, String description,
                     String clinicalSignificance, String aliasesJson,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.canonicalName = canonicalName;
        this.codeLoinc = codeLoinc;
        this.codeSnomed = codeSnomed;
        this.category = category;
        this.standardUnit = standardUnit;
        this.description = description;
        this.clinicalSignificance = clinicalSignificance;
        this.aliasesJson = aliasesJson != null ? aliasesJson : "[]";
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCanonicalName() { return canonicalName; }
    public void setCanonicalName(String canonicalName) { this.canonicalName = canonicalName; }

    public String getCodeLoinc() { return codeLoinc; }
    public void setCodeLoinc(String codeLoinc) { this.codeLoinc = codeLoinc; }

    public String getCodeSnomed() { return codeSnomed; }
    public void setCodeSnomed(String codeSnomed) { this.codeSnomed = codeSnomed; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStandardUnit() { return standardUnit; }
    public void setStandardUnit(String standardUnit) { this.standardUnit = standardUnit; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getClinicalSignificance() { return clinicalSignificance; }
    public void setClinicalSignificance(String clinicalSignificance) { this.clinicalSignificance = clinicalSignificance; }

    public String getAliasesJson() { return aliasesJson; }
    public void setAliasesJson(String aliasesJson) { this.aliasesJson = aliasesJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static BiomarkerBuilder builder() {
        return new BiomarkerBuilder();
    }

    public static class BiomarkerBuilder {
        private UUID id;
        private String canonicalName;
        private String codeLoinc;
        private String codeSnomed;
        private String category;
        private String standardUnit;
        private String description;
        private String clinicalSignificance;
        private String aliasesJson = "[]";
        private Instant createdAt;
        private Instant updatedAt;

        public BiomarkerBuilder id(UUID id) { this.id = id; return this; }
        public BiomarkerBuilder canonicalName(String canonicalName) { this.canonicalName = canonicalName; return this; }
        public BiomarkerBuilder codeLoinc(String codeLoinc) { this.codeLoinc = codeLoinc; return this; }
        public BiomarkerBuilder codeSnomed(String codeSnomed) { this.codeSnomed = codeSnomed; return this; }
        public BiomarkerBuilder category(String category) { this.category = category; return this; }
        public BiomarkerBuilder standardUnit(String standardUnit) { this.standardUnit = standardUnit; return this; }
        public BiomarkerBuilder description(String description) { this.description = description; return this; }
        public BiomarkerBuilder clinicalSignificance(String clinicalSignificance) { this.clinicalSignificance = clinicalSignificance; return this; }
        public BiomarkerBuilder aliasesJson(String aliasesJson) { this.aliasesJson = aliasesJson; return this; }
        public BiomarkerBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public BiomarkerBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Biomarker build() {
            return new Biomarker(id, canonicalName, codeLoinc, codeSnomed, category, standardUnit, description, clinicalSignificance, aliasesJson, createdAt, updatedAt);
        }
    }
}
