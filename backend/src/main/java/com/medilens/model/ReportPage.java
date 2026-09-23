package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "report_pages", uniqueConstraints = {
    @UniqueConstraint(name = "uq_report_page", columnNames = {"report_id", "page_number"})
})
public class ReportPage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "image_storage_path", nullable = false, length = 1024)
    private String imageStoragePath;

    @Column(name = "ocr_raw_text", columnDefinition = "TEXT")
    private String ocrRawText;

    @Column(name = "ocr_confidence_score", precision = 5, scale = 4)
    private BigDecimal ocrConfidenceScore;

    @Column(name = "ocr_engine_used", length = 50)
    private String ocrEngineUsed = "paddleocr";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ReportPage() {}

    public ReportPage(UUID id, Report report, Integer pageNumber, String imageStoragePath,
                      String ocrRawText, BigDecimal ocrConfidenceScore, String ocrEngineUsed,
                      Instant createdAt) {
        this.id = id;
        this.report = report;
        this.pageNumber = pageNumber;
        this.imageStoragePath = imageStoragePath;
        this.ocrRawText = ocrRawText;
        this.ocrConfidenceScore = ocrConfidenceScore;
        this.ocrEngineUsed = ocrEngineUsed != null ? ocrEngineUsed : "paddleocr";
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Report getReport() { return report; }
    public void setReport(Report report) { this.report = report; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getImageStoragePath() { return imageStoragePath; }
    public void setImageStoragePath(String imageStoragePath) { this.imageStoragePath = imageStoragePath; }

    public String getOcrRawText() { return ocrRawText; }
    public void setOcrRawText(String ocrRawText) { this.ocrRawText = ocrRawText; }

    public BigDecimal getOcrConfidenceScore() { return ocrConfidenceScore; }
    public void setOcrConfidenceScore(BigDecimal ocrConfidenceScore) { this.ocrConfidenceScore = ocrConfidenceScore; }

    public String getOcrEngineUsed() { return ocrEngineUsed; }
    public void setOcrEngineUsed(String ocrEngineUsed) { this.ocrEngineUsed = ocrEngineUsed; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static ReportPageBuilder builder() {
        return new ReportPageBuilder();
    }

    public static class ReportPageBuilder {
        private UUID id;
        private Report report;
        private Integer pageNumber;
        private String imageStoragePath;
        private String ocrRawText;
        private BigDecimal ocrConfidenceScore;
        private String ocrEngineUsed = "paddleocr";
        private Instant createdAt;

        public ReportPageBuilder id(UUID id) { this.id = id; return this; }
        public ReportPageBuilder report(Report report) { this.report = report; return this; }
        public ReportPageBuilder pageNumber(Integer pageNumber) { this.pageNumber = pageNumber; return this; }
        public ReportPageBuilder imageStoragePath(String imageStoragePath) { this.imageStoragePath = imageStoragePath; return this; }
        public ReportPageBuilder ocrRawText(String ocrRawText) { this.ocrRawText = ocrRawText; return this; }
        public ReportPageBuilder ocrConfidenceScore(BigDecimal ocrConfidenceScore) { this.ocrConfidenceScore = ocrConfidenceScore; return this; }
        public ReportPageBuilder ocrEngineUsed(String ocrEngineUsed) { this.ocrEngineUsed = ocrEngineUsed; return this; }
        public ReportPageBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ReportPage build() {
            return new ReportPage(id, report, pageNumber, imageStoragePath, ocrRawText, ocrConfidenceScore, ocrEngineUsed, createdAt);
        }
    }
}
