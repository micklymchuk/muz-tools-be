package com.muztools.youtubewav.application.port.out;

import java.nio.file.Path;

public interface WavTranscoder {

    Path transcode(Path sourceFile, Path outputDirectory, String targetBaseName);
}
