package com.muztools.youtubewav.infrastructure.storage;

import com.muztools.common.exception.NotFoundException;
import com.muztools.youtubewav.application.port.out.ConversionStorage;
import com.muztools.youtubewav.config.YouTubeWavProperties;
import com.muztools.youtubewav.domain.StoredConversionFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
public class FileSystemConversionStorage implements ConversionStorage {

    private final Path storageRoot;

    public FileSystemConversionStorage(YouTubeWavProperties properties) {
        this.storageRoot = Path.of(properties.getStorageRoot()).toAbsolutePath().normalize();
    }

    @Override
    public StoredConversionFile store(UUID jobId, Path sourceFile, String preferredFilename) {
        try {
            Path jobDirectory = Files.createDirectories(storageRoot.resolve(jobId.toString()));
            Path targetFile = jobDirectory.resolve(preferredFilename).normalize();
            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return new StoredConversionFile(targetFile.getFileName().toString(), targetFile);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store converted WAV file", exception);
        }
    }

    @Override
    public Resource loadAsResource(UUID jobId, String filename) {
        Path file = storageRoot.resolve(jobId.toString()).resolve(filename).normalize();
        if (!Files.exists(file)) {
            throw new NotFoundException("Converted file not found for job: " + jobId);
        }
        return new FileSystemResource(file);
    }
}
