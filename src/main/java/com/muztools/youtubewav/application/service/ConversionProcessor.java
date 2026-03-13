package com.muztools.youtubewav.application.service;

import com.muztools.common.exception.NotFoundException;
import com.muztools.youtubewav.application.port.out.ConversionJobRepository;
import com.muztools.youtubewav.application.port.out.ConversionStorage;
import com.muztools.youtubewav.application.port.out.WavTranscoder;
import com.muztools.youtubewav.application.port.out.YouTubeAudioDownloader;
import com.muztools.youtubewav.domain.ConversionJob;
import com.muztools.youtubewav.domain.DownloadedAudio;
import com.muztools.youtubewav.domain.StoredConversionFile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class ConversionProcessor {

    private final ConversionJobRepository conversionJobRepository;
    private final YouTubeAudioDownloader audioDownloader;
    private final WavTranscoder wavTranscoder;
    private final ConversionStorage conversionStorage;

    public ConversionProcessor(ConversionJobRepository conversionJobRepository, YouTubeAudioDownloader audioDownloader,
                               WavTranscoder wavTranscoder, ConversionStorage conversionStorage) {
        this.conversionJobRepository = conversionJobRepository;
        this.audioDownloader = audioDownloader;
        this.wavTranscoder = wavTranscoder;
        this.conversionStorage = conversionStorage;
    }

    @Async("conversionTaskExecutor")
    public void processAsync(UUID jobId) {
        ConversionJob job = conversionJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Conversion job not found: " + jobId));

        job.markRunning();
        conversionJobRepository.save(job);

        Path workingDirectory = null;
        try {
            workingDirectory = Files.createTempDirectory("youtube-wav-" + jobId + "-");
            DownloadedAudio downloadedAudio = audioDownloader.download(job.getSourceUrl(), workingDirectory);
            String targetBaseName = sanitizeFilename(downloadedAudio.title());
            Path wavFile = wavTranscoder.transcode(downloadedAudio.filePath(), workingDirectory, targetBaseName);
            StoredConversionFile storedFile = conversionStorage.store(jobId, wavFile, targetBaseName + ".wav");
            job.markCompleted(storedFile.filename());
            conversionJobRepository.save(job);
        } catch (Exception exception) {
            job.markFailed(exception.getMessage());
            conversionJobRepository.save(job);
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
