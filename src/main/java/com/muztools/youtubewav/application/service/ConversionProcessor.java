package com.muztools.youtubewav.application.service;

import com.muztools.common.exception.NotFoundException;
import com.muztools.youtubewav.application.port.out.ConversionJobRepository;
import com.muztools.youtubewav.application.port.out.ConversionStorage;
import com.muztools.youtubewav.application.port.out.YouTubeAudioDownloader;
import com.muztools.youtubewav.domain.ConversionJob;
import com.muztools.youtubewav.domain.DownloadedAudio;
import com.muztools.youtubewav.domain.StoredConversionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class ConversionProcessor {

    private static final Logger log = LoggerFactory.getLogger(ConversionProcessor.class);

    private final ConversionJobRepository conversionJobRepository;
    private final YouTubeAudioDownloader audioDownloader;
    private final ConversionStorage conversionStorage;

    public ConversionProcessor(ConversionJobRepository conversionJobRepository, YouTubeAudioDownloader audioDownloader,
                               ConversionStorage conversionStorage) {
        this.conversionJobRepository = conversionJobRepository;
        this.audioDownloader = audioDownloader;
        this.conversionStorage = conversionStorage;
    }

    @Async("conversionTaskExecutor")
    public void processAsync(UUID jobId) {
        ConversionJob job = conversionJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Conversion job not found: " + jobId));

        log.info("Starting conversion job {} for source {}", jobId, job.getSourceUrl());
        job.markRunning();
        conversionJobRepository.save(job);

        Path workingDirectory = null;
        try {
            workingDirectory = Files.createTempDirectory("youtube-wav-" + jobId + "-");
            log.info("Created working directory {} for job {}", workingDirectory, jobId);
            DownloadedAudio downloadedAudio = audioDownloader.download(job.getSourceUrl(), workingDirectory);
            String targetBaseName = sanitizeFilename(downloadedAudio.title());
            StoredConversionFile storedFile = conversionStorage.store(jobId, downloadedAudio.filePath(), targetBaseName + ".wav");
            job.markCompleted(storedFile.filename());
            conversionJobRepository.save(job);
            log.info("Completed conversion job {} with stored file {}", jobId, storedFile.filename());
        } catch (Exception exception) {
            job.markFailed(exception.getMessage());
            conversionJobRepository.save(job);
            log.error("Conversion job {} failed: {}", jobId, exception.getMessage(), exception);
        } finally {
            if (workingDirectory != null) {
                deleteQuietly(workingDirectory);
            }
        }
    }

    private String sanitizeFilename(String value) {
        String sanitized = value == null ? "converted-audio" : value
                .replaceAll("[^a-zA-Z0-9-_\\.]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^[_\\.]+|[_\\.]+$", "");
        return sanitized.isBlank() ? "converted-audio" : sanitized;
    }

    private void deleteQuietly(Path directory) {
        try (var paths = Files.walk(directory)) {
            paths.sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }
    }
}
