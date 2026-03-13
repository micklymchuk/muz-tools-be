package com.muztools.youtubewav.domain;

import java.time.Instant;
import java.util.UUID;

public class ConversionJob {

    private final UUID id;
    private final String sourceUrl;
    private final Instant createdAt;
    private volatile ConversionStatus status;
    private volatile Instant updatedAt;
    private volatile String failureReason;
    private volatile String outputFilename;

    public ConversionJob(UUID id, String sourceUrl, Instant createdAt, ConversionStatus status, Instant updatedAt,
                         String failureReason, String outputFilename) {
        this.id = id;
        this.sourceUrl = sourceUrl;
        this.createdAt = createdAt;
        this.status = status;
        this.updatedAt = updatedAt;
        this.failureReason = failureReason;
        this.outputFilename = outputFilename;
    }

    public static ConversionJob create(String sourceUrl) {
        Instant now = Instant.now();
        return new ConversionJob(UUID.randomUUID(), sourceUrl, now, ConversionStatus.PENDING, now, null, null);
    }

    public void markRunning() {
        status = ConversionStatus.RUNNING;
        updatedAt = Instant.now();
        failureReason = null;
    }

    public void markCompleted(String outputFilename) {
        status = ConversionStatus.COMPLETED;
        updatedAt = Instant.now();
        this.outputFilename = outputFilename;
        failureReason = null;
    }

    public void markFailed(String failureReason) {
        status = ConversionStatus.FAILED;
        updatedAt = Instant.now();
        this.failureReason = failureReason;
    }

    public UUID getId() {
        return id;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ConversionStatus getStatus() {
        return status;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getOutputFilename() {
        return outputFilename;
    }
}
