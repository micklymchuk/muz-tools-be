package com.muztools.youtubewav.infrastructure.process;

import com.muztools.youtubewav.application.port.out.YouTubeAudioDownloader;
import com.muztools.youtubewav.config.YouTubeWavProperties;
import com.muztools.youtubewav.domain.DownloadedAudio;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class YtDlpYouTubeAudioDownloader implements YouTubeAudioDownloader {

    private final YouTubeWavProperties properties;
    private final CommandRunner commandRunner;

    public YtDlpYouTubeAudioDownloader(YouTubeWavProperties properties, CommandRunner commandRunner) {
        this.properties = properties;
        this.commandRunner = commandRunner;
    }

    @Override
    public DownloadedAudio download(String url, Path workingDirectory) {
        Path downloadDirectory = createDirectory(workingDirectory.resolve("download"));
        String outputTemplate = downloadDirectory.resolve("%(title)s.%(ext)s").toAbsolutePath().toString();

        List<String> command = new ArrayList<>(List.of(
                properties.getYtDlpCommand(),
                "-f", "bestaudio/best",
                "--no-playlist",
                "-o", outputTemplate
        ));

        if (properties.getYtDlpJsRuntimes() != null && !properties.getYtDlpJsRuntimes().isBlank()) {
            command.add("--js-runtimes");
            command.add(properties.getYtDlpJsRuntimes());
        }

        if (properties.getYtDlpExtractorArgs() != null && !properties.getYtDlpExtractorArgs().isBlank()) {
            command.add("--extractor-args");
            command.add(properties.getYtDlpExtractorArgs());
        }

        if (properties.getYtDlpCookiesFile() != null && !properties.getYtDlpCookiesFile().isBlank()) {
            command.add("--cookies");
            command.add(properties.getYtDlpCookiesFile());
        }

        command.add(url);

        commandRunner.run(
                command,
                workingDirectory,
                Duration.ofSeconds(properties.getDownloadTimeoutSeconds())
        );

        Path downloadedFile = resolveSingleFile(downloadDirectory);
        String fileName = downloadedFile.getFileName().toString();
        int extensionSeparator = fileName.lastIndexOf('.');
        String title = extensionSeparator > 0 ? fileName.substring(0, extensionSeparator) : fileName;
        return new DownloadedAudio(downloadedFile, title);
    }

    private Path createDirectory(Path directory) {
        try {
            return Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new CommandExecutionException("Failed to create working directory");
        }
    }

    private Path resolveSingleFile(Path directory) {
        try (var files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .max(Comparator.comparingLong(this::safeSize))
                    .orElseThrow(() -> new CommandExecutionException("yt-dlp did not produce an audio file"));
        } catch (IOException exception) {
            throw new CommandExecutionException("Failed to inspect downloaded audio");
        }
    }

    private long safeSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException exception) {
            return -1;
        }
    }
}
