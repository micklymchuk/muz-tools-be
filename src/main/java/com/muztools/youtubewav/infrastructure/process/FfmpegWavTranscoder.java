package com.muztools.youtubewav.infrastructure.process;

import com.muztools.youtubewav.application.port.out.WavTranscoder;
import com.muztools.youtubewav.config.YouTubeWavProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@Component
public class FfmpegWavTranscoder implements WavTranscoder {

    private final YouTubeWavProperties properties;
    private final CommandRunner commandRunner;

    public FfmpegWavTranscoder(YouTubeWavProperties properties, CommandRunner commandRunner) {
        this.properties = properties;
        this.commandRunner = commandRunner;
    }

    @Override
    public Path transcode(Path sourceFile, Path outputDirectory, String targetBaseName) {
        Path transcodeDirectory = createDirectory(outputDirectory.resolve("transcoded"));
        Path outputFile = transcodeDirectory.resolve(targetBaseName + ".wav");

        commandRunner.run(
                List.of(
                        properties.getFfmpegCommand(),
                        "-y",
                        "-i",
                        sourceFile.toAbsolutePath().toString(),
                        "-vn",
                        "-acodec",
                        "pcm_s16le",
                        "-ar",
                        String.valueOf(properties.getSampleRate()),
                        "-ac",
                        String.valueOf(properties.getChannels()),
                        outputFile.toAbsolutePath().toString()
                ),
                outputDirectory,
                Duration.ofSeconds(properties.getTranscodeTimeoutSeconds())
        );

        if (!Files.exists(outputFile)) {
            throw new CommandExecutionException("ffmpeg did not produce a WAV file");
        }

        return outputFile;
    }

    private Path createDirectory(Path directory) {
        try {
            return Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new CommandExecutionException("Failed to create transcoding directory");
        }
    }
}
