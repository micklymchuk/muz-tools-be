package com.muztools.youtubewav.web;

import com.muztools.youtubewav.domain.ConversionJob;

import java.time.Instant;
import java.util.UUID;

public record ConversionJobResponse(
        UUID jobId,
        String sourceUrl,
        String status,
        String failureReason,
        String outputFilename,
        Instant createdAt,
        Instant updatedAt,
        String downloadUrl
) {

    public static ConversionJobResponse from(ConversionJob job, String downloadUrl) {
        return new ConversionJobResponse(
                job.getId(),
                job.getSourceUrl(),
                job.getStatus().name(),
                job.getFailureReason(),
                job.getOutputFilename(),
                job.getCreatedAt(),
                job.getUpdatedAt(),
                downloadUrl
        );
    }
}
