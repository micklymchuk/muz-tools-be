package com.muztools.youtubewav.application.port.out;

import com.muztools.youtubewav.domain.StoredConversionFile;
import org.springframework.core.io.Resource;

import java.nio.file.Path;
import java.util.UUID;

public interface ConversionStorage {

    StoredConversionFile store(UUID jobId, Path sourceFile, String preferredFilename);

    Resource loadAsResource(UUID jobId, String filename);
}
