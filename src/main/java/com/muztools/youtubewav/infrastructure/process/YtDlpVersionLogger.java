package com.muztools.youtubewav.infrastructure.process;

import com.muztools.youtubewav.config.YouTubeWavProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Component
public class YtDlpVersionLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(YtDlpVersionLogger.class);

    private final YouTubeWavProperties properties;
    private final CommandRunner commandRunner;

    public YtDlpVersionLogger(YouTubeWavProperties properties, CommandRunner commandRunner) {
        this.properties = properties;
        this.commandRunner = commandRunner;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String version = commandRunner.runAndCapture(
                    List.of(properties.getYtDlpCommand(), "--version"),
                    Path.of(".").toAbsolutePath().normalize(),
                    Duration.ofSeconds(15)
            );
            log.info("yt-dlp version: {}", version);
        } catch (CommandExecutionException exception) {
            log.warn("Failed to read yt-dlp version at startup: {}", exception.getMessage());
        }
    }
}
