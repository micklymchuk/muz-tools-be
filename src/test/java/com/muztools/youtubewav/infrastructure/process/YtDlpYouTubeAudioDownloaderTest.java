package com.muztools.youtubewav.infrastructure.process;

import com.muztools.youtubewav.config.YouTubeWavProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class YtDlpYouTubeAudioDownloaderTest {

    @TempDir
    Path tempDir;

    @Mock
    private CommandRunner commandRunner;

    @Test
    void downloadShouldIncludeSleepAndCookiesArguments() throws Exception {
        YouTubeWavProperties properties = new YouTubeWavProperties();
        properties.setYtDlpSleepRequestsSeconds(5);
        properties.setYtDlpSleepIntervalSeconds(5);
        properties.setYtDlpMaxSleepIntervalSeconds(10);
        Path cookiesFile = Files.writeString(tempDir.resolve("youtube-cookies.txt"), "cookies");
        properties.setYtDlpCookiesFile(cookiesFile.toString());

        doAnswer(invocation -> {
            Path workingDirectory = invocation.getArgument(1);
            Path downloadDirectory = Files.createDirectories(workingDirectory.resolve("download"));
            Files.writeString(downloadDirectory.resolve("demo.wav"), "audio");
            return null;
        }).when(commandRunner).run(any(), any(), any());

        YtDlpYouTubeAudioDownloader downloader = new YtDlpYouTubeAudioDownloader(properties, commandRunner);

        downloader.download("https://www.youtube.com/watch?v=test123", tempDir.resolve("job"));

        ArgumentCaptor<List<String>> commandCaptor = ArgumentCaptor.forClass(List.class);
        verify(commandRunner).run(commandCaptor.capture(), eq(tempDir.resolve("job")), eq(Duration.ofSeconds(600)));
        assertThat(commandCaptor.getValue()).containsSubsequence(
                List.of(
                        "yt-dlp",
                        "--no-cookies",
                        "-x",
                        "--audio-format", "wav",
                        "-f", "bestaudio/best",
                        "--no-playlist",
                        "-o"
                )
        );
        assertThat(commandCaptor.getValue()).containsSubsequence("--sleep-requests", "5");
        assertThat(commandCaptor.getValue()).containsSubsequence("--sleep-interval", "5");
        assertThat(commandCaptor.getValue()).containsSubsequence("--max-sleep-interval", "10");
        assertThat(commandCaptor.getValue()).containsSubsequence("--cookies", cookiesFile.toAbsolutePath().toString());
        assertThat(commandCaptor.getValue()).contains("https://www.youtube.com/watch?v=test123");
    }

    @Test
    void downloadShouldFailFastWhenCookiesFileIsUnreadable() {
        YouTubeWavProperties properties = new YouTubeWavProperties();
        properties.setYtDlpCookiesFile(tempDir.resolve("missing-cookies.txt").toString());

        YtDlpYouTubeAudioDownloader downloader = new YtDlpYouTubeAudioDownloader(properties, commandRunner);

        assertThatThrownBy(() -> downloader.download("https://www.youtube.com/watch?v=test123", tempDir.resolve("job")))
                .isInstanceOf(CommandExecutionException.class)
                .hasMessageContaining("cookies file is not readable");
    }
}
