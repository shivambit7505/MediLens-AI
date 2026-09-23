package com.medilens.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "storage_path", nullable = false, length = 1024)
    private String storagePath;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "file_hash_sha256", nullable = false, length = 64)
    private String fileHashSha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportStatus status = ReportStatus.UPLOADED;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "page_count")
    private Integer pageCount = 0;

    @Column(name = "processing_started_at")
    private Instant processingStartedAt;

    @Column(name = "processing_completed_at")
    private Instant processingCompletedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Report() {}

    public Report(UUID id, User user, String originalFilename, String storagePath, String mimeType,
                  Long fileSizeBytes, String fileHashSha256, ReportStatus status, String failureReason,
                  Integer pageCount, Instant processingStartedAt, Instant processingCompletedAt,
                  Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.user = user;
        this.originalFilename = originalFilename;
        this.storagePath = storagePath;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.fileHashSha256 = fileHashSha256;
        this.status = status != null ? status : ReportStatus.UPLOADED;
        this.failureReason = failureReason;
        this.pageCount = pageCount != null ? pageCount : 0;
        this.processingStartedAt = processingStartedAt;
        this.processingCompletedAt = processingCompletedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoragePath() { return storagePath; }
    public void setStoragePath(String storagePath) { this.storagePath = storagePath; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getFileHashSha256() { return fileHashSha256; }
    public void setFileHashSha256(String fileHashSha256) { this.fileHashSha256 = fileHashSha256; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public Instant getProcessingStartedAt() { return processingStartedAt; }
    public void setProcessingStartedAt(Instant processingStartedAt) { this.processingStartedAt = processingStartedAt; }

    public Instant getProcessingCompletedAt() { return processingCompletedAt; }
    public void setProcessingCompletedAt(Instant processingCompletedAt) { this.processingCompletedAt = processingCompletedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static ReportBuilder builder() {
        return new ReportBuilder();
    }

    public static class ReportBuilder {
        private UUID id;
        private User user;
        private String originalFilename;
        private String storagePath;
        private String mimeType;
        private Long fileSizeBytes;
        private String fileHashSha256;
        private ReportStatus status = ReportStatus.UPLOADED;
        private String failureReason;
        private Integer pageCount = 0;
        private Instant processingStartedAt;
        private Instant processingCompletedAt;
        private Instant createdAt;
        private Instant updatedAt;

        public ReportBuilder id(UUID id) { this.id = id; return this; }
        public ReportBuilder user(User user) { this.user = user; return this; }
        public ReportBuilder originalFilename(String originalFilename) { this.originalFilename = originalFilename; return this; }
        public ReportBuilder storagePath(String storagePath) { this.storagePath = storagePath; return this; }
        public ReportBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public ReportBuilder fileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; return this; }
        public ReportBuilder fileHashSha256(String fileHashSha256) { this.fileHashSha256 = fileHashSha256; return this; }
        public ReportBuilder status(ReportStatus status) { this.status = status; return this; }
        public ReportBuilder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public ReportBuilder pageCount(Integer pageCount) { this.pageCount = pageCount; return this; }
        public ReportBuilder processingStartedAt(Instant processingStartedAt) { this.processingStartedAt = processingStartedAt; return this; }
        public ReportBuilder processingCompletedAt(Instant processingCompletedAt) { this.processingCompletedAt = processingCompletedAt; return this; }
        public ReportBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public ReportBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public Report build() {
            return new Report(id, user, originalFilename, storagePath, mimeType, fileSizeBytes, fileHashSha256, status, failureReason, pageCount, processingStartedAt, processingCompletedAt, createdAt, updatedAt);
        }
    }
}
