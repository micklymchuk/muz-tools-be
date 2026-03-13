package com.muztools.youtubewav.web;

import com.muztools.youtubewav.application.service.ConverterCapabilities;

public record ConverterCapabilitiesResponse(boolean ready, boolean ytDlpAvailable, boolean ffmpegAvailable) {

    public static ConverterCapabilitiesResponse from(ConverterCapabilities capabilities) {
        return new ConverterCapabilitiesResponse(
                capabilities.ready(),
                capabilities.ytDlpAvailable(),
                capabilities.ffmpegAvailable()
        );
    }
}
