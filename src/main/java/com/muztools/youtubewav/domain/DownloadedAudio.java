package com.muztools.youtubewav.domain;

import java.nio.file.Path;

public record DownloadedAudio(Path filePath, String title) {
}
