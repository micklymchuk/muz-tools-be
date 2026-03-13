package com.muztools.youtubewav.application.service;

import com.muztools.youtubewav.config.YouTubeWavProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ConverterCapabilitiesService {

    private final YouTubeWavProperties properties;

    public ConverterCapabilitiesService(YouTubeWavProperties properties) {
        this.properties = properties;
    }

    public ConverterCapabilities getCapabilities() {
        return new ConverterCapabilities(
                isCommandAvailable(properties.getYtDlpCommand()),
                isCommandAvailable(properties.getFfmpegCommand())
        );
    }

    private boolean isCommandAvailable(String command) {
        Process process = null;
        try {
            process = new ProcessBuilder(List.of(command, "-version"))
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }
}
