package com.muztools.youtubewav.application.service;

public record ConverterCapabilities(boolean ytDlpAvailable, boolean ffmpegAvailable) {

    public boolean ready() {
        return ytDlpAvailable && ffmpegAvailable;
    }
}
