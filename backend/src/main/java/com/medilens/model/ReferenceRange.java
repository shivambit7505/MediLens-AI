package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reference_ranges")
public class ReferenceRange {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biomarker_id", nullable = false)
    private Biomarker biomarker;

    @Column(name = "gender_applicable", nullable = false, length = 20)
    private String genderApplicable = "ALL";

    @Column(name = "age_min_years", precision = 5, scale = 2, nullable = false)
    private BigDecimal ageMinYears = BigDecimal.ZERO;

    @Column(name = "age_max_years", precision = 5, scale = 2, nullable = false)
    private BigDecimal ageMaxYears = new BigDecimal("150.0");

    @Column(nullable = false, length = 50)
    private String unit;

    @Column(name = "low_value", precision = 12, scale = 4, nullable = false)
    private BigDecimal lowValue;

    @Column(name = "high_value", precision = 12, scale = 4, nullable = false)
    private BigDecimal highValue;

    @Column(name = "critical_low", precision = 12, scale = 4)
    private BigDecimal criticalLow;

    @Column(name = "critical_high", precision = 12, scale = 4)
    private BigDecimal criticalHigh;

    @Column(name = "source_citation", nullable = false)
    private String sourceCitation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ReferenceRange() {}

    public ReferenceRange(UUID id, Biomarker biomarker, String genderApplicable, BigDecimal ageMinYears,
                          BigDecimal ageMaxYears, String unit, BigDecimal lowValue, BigDecimal highValue,
                          BigDecimal criticalLow, BigDecimal criticalHigh, String sourceCitation,
                          Instant createdAt) {
        this.id = id;
        this.biomarker = biomarker;
        this.genderApplicable = genderApplicable != null ? genderApplicable : "ALL";
        this.ageMinYears = ageMinYears != null ? ageMinYears : BigDecimal.ZERO;
        this.ageMaxYears = ageMaxYears != null ? ageMaxYears : new BigDecimal("150.0");
        this.unit = unit;
        this.lowValue = lowValue;
        this.highValue = highValue;
        this.criticalLow = criticalLow;
        this.criticalHigh = criticalHigh;
        this.sourceCitation = sourceCitation;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Biomarker getBiomarker() { return biomarker; }
    public void setBiomarker(Biomarker biomarker) { this.biomarker = biomarker; }

    public String getGenderApplicable() { return genderApplicable; }
    public void setGenderApplicable(String genderApplicable) { this.genderApplicable = genderApplicable; }

    public BigDecimal getAgeMinYears() { return ageMinYears; }
    public void setAgeMinYears(BigDecimal ageMinYears) { this.ageMinYears = ageMinYears; }

    public BigDecimal getAgeMaxYears() { return ageMaxYears; }
    public void setAgeMaxYears(BigDecimal ageMaxYears) { this.ageMaxYears = ageMaxYears; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public BigDecimal getLowValue() { return lowValue; }
    public void setLowValue(BigDecimal lowValue) { this.lowValue = lowValue; }

    public BigDecimal getHighValue() { return highValue; }
    public void setHighValue(BigDecimal highValue) { this.highValue = highValue; }

    public BigDecimal getCriticalLow() { return criticalLow; }
    public void setCriticalLow(BigDecimal criticalLow) { this.criticalLow = criticalLow; }

    public BigDecimal getCriticalHigh() { return criticalHigh; }
    public void setCriticalHigh(BigDecimal criticalHigh) { this.criticalHigh = criticalHigh; }

    public String getSourceCitation() { return sourceCitation; }
    public void setSourceCitation(String sourceCitation) { this.sourceCitation = sourceCitation; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static ReferenceRangeBuilder builder() {
        return new ReferenceRangeBuilder();
    }

    public static class ReferenceRangeBuilder {
        private UUID id;
        private Biomarker biomarker;
        private String genderApplicable = "ALL";
        private BigDecimal ageMinYears = BigDecimal.ZERO;
        private BigDecimal ageMaxYears = new BigDecimal("150.0");
        private String unit;
        private BigDecimal lowValue;
        private BigDecimal highValue;
        private BigDecimal criticalLow;
        private BigDecimal criticalHigh;
        private String sourceCitation;
        private Instant createdAt;

        public ReferenceRangeBuilder id(UUID id) { this.id = id; return this; }
        public ReferenceRangeBuilder biomarker(Biomarker biomarker) { this.biomarker = biomarker; return this; }
        public ReferenceRangeBuilder genderApplicable(String genderApplicable) { this.genderApplicable = genderApplicable; return this; }
        public ReferenceRangeBuilder ageMinYears(BigDecimal ageMinYears) { this.ageMinYears = ageMinYears; return this; }
        public ReferenceRangeBuilder ageMaxYears(BigDecimal ageMaxYears) { this.ageMaxYears = ageMaxYears; return this; }
        public ReferenceRangeBuilder unit(String unit) { this.unit = unit; return this; }
        public ReferenceRangeBuilder lowValue(BigDecimal lowValue) { this.lowValue = lowValue; return this; }
        public ReferenceRangeBuilder highValue(BigDecimal highValue) { this.highValue = highValue; return this; }
        public ReferenceRangeBuilder criticalLow(BigDecimal criticalLow) { this.criticalLow = criticalLow; return this; }
        public ReferenceRangeBuilder criticalHigh(BigDecimal criticalHigh) { this.criticalHigh = criticalHigh; return this; }
        public ReferenceRangeBuilder sourceCitation(String sourceCitation) { this.sourceCitation = sourceCitation; return this; }
        public ReferenceRangeBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ReferenceRange build() {
            return new ReferenceRange(id, biomarker, genderApplicable, ageMinYears, ageMaxYears, unit, lowValue, highValue, criticalLow, criticalHigh, sourceCitation, createdAt);
        }
    }
}
