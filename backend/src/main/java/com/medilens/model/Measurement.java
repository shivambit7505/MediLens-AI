package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_page_id")
    private ReportPage reportPage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "biomarker_id", nullable = false)
    private Biomarker biomarker;

    @Column(name = "extracted_name", nullable = false)
    private String extractedName;

    @Column(name = "observed_value_raw", nullable = false, length = 100)
    private String observedValueRaw;

    @Column(name = "observed_value_numeric", precision = 14, scale = 4)
    private BigDecimal observedValueNumeric;

    @Column(name = "extracted_unit", length = 50)
    private String extractedUnit;

    @Column(name = "normalized_value_numeric", precision = 14, scale = 4, nullable = false)
    private BigDecimal normalizedValueNumeric;

    @Column(name = "normalized_unit", nullable = false, length = 50)
    private String normalizedUnit;

    @Column(name = "extracted_reference_text")
    private String extractedReferenceText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_reference_range_id")
    private ReferenceRange matchedReferenceRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MeasurementStatus status;

    @Column(precision = 5, scale = 4, nullable = false)
    private BigDecimal confidence;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber = 1;

    @Column(name = "source_text_snippet", nullable = false, columnDefinition = "TEXT")
    private String sourceTextSnippet;

    @Column(name = "bounding_box_json", columnDefinition = "TEXT")
    private String boundingBoxJson;

    @Column(name = "measurement_date")
    private LocalDate measurementDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Measurement() {}

    public Measurement(UUID id, Report report, ReportPage reportPage, User user, Biomarker biomarker,
                       String extractedName, String observedValueRaw, BigDecimal observedValueNumeric,
                       String extractedUnit, BigDecimal normalizedValueNumeric, String normalizedUnit,
                       String extractedReferenceText, ReferenceRange matchedReferenceRange,
                       MeasurementStatus status, BigDecimal confidence, Integer pageNumber,
                       String sourceTextSnippet, String boundingBoxJson, LocalDate measurementDate,
                       Instant createdAt) {
        this.id = id;
        this.report = report;
        this.reportPage = reportPage;
        this.user = user;
        this.biomarker = biomarker;
        this.extractedName = extractedName;
        this.observedValueRaw = observedValueRaw;
        this.observedValueNumeric = observedValueNumeric;
        this.extractedUnit = extractedUnit;
        this.normalizedValueNumeric = normalizedValueNumeric;
        this.normalizedUnit = normalizedUnit;
        this.extractedReferenceText = extractedReferenceText;
        this.matchedReferenceRange = matchedReferenceRange;
        this.status = status;
        this.confidence = confidence;
        this.pageNumber = pageNumber != null ? pageNumber : 1;
        this.sourceTextSnippet = sourceTextSnippet;
        this.boundingBoxJson = boundingBoxJson;
        this.measurementDate = measurementDate;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public ReportPage getReportPage() { return reportPage; }
    public void setReportPage(ReportPage reportPage) { this.reportPage = reportPage; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Biomarker getBiomarker() { return biomarker; }
    public void setBiomarker(Biomarker biomarker) { this.biomarker = biomarker; }

    public String getExtractedName() { return extractedName; }
    public void setExtractedName(String extractedName) { this.extractedName = extractedName; }

    public String getObservedValueRaw() { return observedValueRaw; }
    public void setObservedValueRaw(String observedValueRaw) { this.observedValueRaw = observedValueRaw; }

    public BigDecimal getObservedValueNumeric() { return observedValueNumeric; }
    public void setObservedValueNumeric(BigDecimal observedValueNumeric) { this.observedValueNumeric = observedValueNumeric; }

    public String getExtractedUnit() { return extractedUnit; }
    public void setExtractedUnit(String extractedUnit) { this.extractedUnit = extractedUnit; }

    public BigDecimal getNormalizedValueNumeric() { return normalizedValueNumeric; }
    public void setNormalizedValueNumeric(BigDecimal normalizedValueNumeric) { this.normalizedValueNumeric = normalizedValueNumeric; }

    public String getNormalizedUnit() { return normalizedUnit; }
    public void setNormalizedUnit(String normalizedUnit) { this.normalizedUnit = normalizedUnit; }

    public String getExtractedReferenceText() { return extractedReferenceText; }
    public void setExtractedReferenceText(String extractedReferenceText) { this.extractedReferenceText = extractedReferenceText; }

    public ReferenceRange getMatchedReferenceRange() { return matchedReferenceRange; }
    public void setMatchedReferenceRange(ReferenceRange matchedReferenceRange) { this.matchedReferenceRange = matchedReferenceRange; }

    public MeasurementStatus getStatus() { return status; }
    public void setStatus(MeasurementStatus status) { this.status = status; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getSourceTextSnippet() { return sourceTextSnippet; }
    public void setSourceTextSnippet(String sourceTextSnippet) { this.sourceTextSnippet = sourceTextSnippet; }

    public String getBoundingBoxJson() { return boundingBoxJson; }
    public void setBoundingBoxJson(String boundingBoxJson) { this.boundingBoxJson = boundingBoxJson; }

    public LocalDate getMeasurementDate() { return measurementDate; }
    public void setMeasurementDate(LocalDate measurementDate) { this.measurementDate = measurementDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static MeasurementBuilder builder() {
        return new MeasurementBuilder();
    }

    public static class MeasurementBuilder {
        private UUID id;
        private Report report;
        private ReportPage reportPage;
        private User user;
        private Biomarker biomarker;
        private String extractedName;
        private String observedValueRaw;
        private BigDecimal observedValueNumeric;
        private String extractedUnit;
        private BigDecimal normalizedValueNumeric;
        private String normalizedUnit;
        private String extractedReferenceText;
        private ReferenceRange matchedReferenceRange;
        private MeasurementStatus status;
        private BigDecimal confidence;
        private Integer pageNumber = 1;
        private String sourceTextSnippet;
        private String boundingBoxJson;
        private LocalDate measurementDate;
        private Instant createdAt;

        public MeasurementBuilder id(UUID id) { this.id = id; return this; }
        public MeasurementBuilder report(Report report) { this.report = report; return this; }
        public MeasurementBuilder reportPage(ReportPage reportPage) { this.reportPage = reportPage; return this; }
        public MeasurementBuilder user(User user) { this.user = user; return this; }
        public MeasurementBuilder biomarker(Biomarker biomarker) { this.biomarker = biomarker; return this; }
        public MeasurementBuilder extractedName(String extractedName) { this.extractedName = extractedName; return this; }
        public MeasurementBuilder observedValueRaw(String observedValueRaw) { this.observedValueRaw = observedValueRaw; return this; }
        public MeasurementBuilder observedValueNumeric(BigDecimal observedValueNumeric) { this.observedValueNumeric = observedValueNumeric; return this; }
        public MeasurementBuilder extractedUnit(String extractedUnit) { this.extractedUnit = extractedUnit; return this; }
        public MeasurementBuilder normalizedValueNumeric(BigDecimal normalizedValueNumeric) { this.normalizedValueNumeric = normalizedValueNumeric; return this; }
        public MeasurementBuilder normalizedUnit(String normalizedUnit) { this.normalizedUnit = normalizedUnit; return this; }
        public MeasurementBuilder extractedReferenceText(String extractedReferenceText) { this.extractedReferenceText = extractedReferenceText; return this; }
        public MeasurementBuilder matchedReferenceRange(ReferenceRange matchedReferenceRange) { this.matchedReferenceRange = matchedReferenceRange; return this; }
        public MeasurementBuilder status(MeasurementStatus status) { this.status = status; return this; }
        public MeasurementBuilder confidence(BigDecimal confidence) { this.confidence = confidence; return this; }
        public MeasurementBuilder pageNumber(Integer pageNumber) { this.pageNumber = pageNumber; return this; }
        public MeasurementBuilder sourceTextSnippet(String sourceTextSnippet) { this.sourceTextSnippet = sourceTextSnippet; return this; }
        public MeasurementBuilder boundingBoxJson(String boundingBoxJson) { this.boundingBoxJson = boundingBoxJson; return this; }
        public MeasurementBuilder measurementDate(LocalDate measurementDate) { this.measurementDate = measurementDate; return this; }
        public MeasurementBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public Measurement build() {
            return new Measurement(id, report, reportPage, user, biomarker, extractedName, observedValueRaw, observedValueNumeric, extractedUnit, normalizedValueNumeric, normalizedUnit, extractedReferenceText, matchedReferenceRange, status, confidence, pageNumber, sourceTextSnippet, boundingBoxJson, measurementDate, createdAt);
        }
    }
}
