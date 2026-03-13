package com.muztools.youtubewav.domain;

import java.nio.file.Path;

public record StoredConversionFile(String filename, Path path) {
}
