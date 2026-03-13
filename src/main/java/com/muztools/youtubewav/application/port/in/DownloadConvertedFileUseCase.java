package com.muztools.youtubewav.application.port.in;

import org.springframework.core.io.Resource;

import java.util.UUID;

public interface DownloadConvertedFileUseCase {

    Resource download(UUID jobId);
}
