package com.muztools.youtubewav.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.youtube-wav")
public class YouTubeWavProperties {

    @NotBlank
    private String storageRoot = "build/generated-output/youtube-wav";

    @NotBlank
    private String ytDlpCommand = "yt-dlp";

    private String ytDlpJsRuntimes = "node";

    private String ytDlpExtractorArgs;

    private String ytDlpCookiesFile;

    @Min(0)
    @Max(300)
    private int ytDlpSleepRequestsSeconds;

    @Min(0)
    @Max(300)
    private int ytDlpSleepIntervalSeconds;

    @Min(0)
    @Max(300)
    private int ytDlpMaxSleepIntervalSeconds;

    @NotBlank
    private String ffmpegCommand = "ffmpeg";

    @Min(30)
    @Max(3600)
    private long downloadTimeoutSeconds = 600;

    @Min(30)
    @Max(3600)
    private long transcodeTimeoutSeconds = 600;

    @Min(8_000)
    @Max(192_000)
    private int sampleRate = 44_100;

    @Min(1)
    @Max(2)
    private int channels = 2;

    public String getStorageRoot() {
        return storageRoot;
    }

    public void setStorageRoot(String storageRoot) {
        this.storageRoot = storageRoot;
    }

    public String getYtDlpCommand() {
        return ytDlpCommand;
    }

    public void setYtDlpCommand(String ytDlpCommand) {
        this.ytDlpCommand = ytDlpCommand;
    }

    public String getYtDlpJsRuntimes() {
        return ytDlpJsRuntimes;
    }

    public void setYtDlpJsRuntimes(String ytDlpJsRuntimes) {
        this.ytDlpJsRuntimes = ytDlpJsRuntimes;
    }

    public String getYtDlpExtractorArgs() {
        return ytDlpExtractorArgs;
    }

    public void setYtDlpExtractorArgs(String ytDlpExtractorArgs) {
        this.ytDlpExtractorArgs = ytDlpExtractorArgs;
    }

    public String getYtDlpCookiesFile() {
        return ytDlpCookiesFile;
    }

    public void setYtDlpCookiesFile(String ytDlpCookiesFile) {
        this.ytDlpCookiesFile = ytDlpCookiesFile;
    }

    public int getYtDlpSleepRequestsSeconds() {
        return ytDlpSleepRequestsSeconds;
    }

    public void setYtDlpSleepRequestsSeconds(int ytDlpSleepRequestsSeconds) {
        this.ytDlpSleepRequestsSeconds = ytDlpSleepRequestsSeconds;
    }

    public int getYtDlpSleepIntervalSeconds() {
        return ytDlpSleepIntervalSeconds;
    }

    public void setYtDlpSleepIntervalSeconds(int ytDlpSleepIntervalSeconds) {
        this.ytDlpSleepIntervalSeconds = ytDlpSleepIntervalSeconds;
    }

    public int getYtDlpMaxSleepIntervalSeconds() {
        return ytDlpMaxSleepIntervalSeconds;
    }

    public void setYtDlpMaxSleepIntervalSeconds(int ytDlpMaxSleepIntervalSeconds) {
        this.ytDlpMaxSleepIntervalSeconds = ytDlpMaxSleepIntervalSeconds;
    }

    public String getFfmpegCommand() {
        return ffmpegCommand;
    }

    public void setFfmpegCommand(String ffmpegCommand) {
        this.ffmpegCommand = ffmpegCommand;
    }

    public long getDownloadTimeoutSeconds() {
        return downloadTimeoutSeconds;
    }

    public void setDownloadTimeoutSeconds(long downloadTimeoutSeconds) {
        this.downloadTimeoutSeconds = downloadTimeoutSeconds;
    }

    public long getTranscodeTimeoutSeconds() {
        return transcodeTimeoutSeconds;
    }

    public void setTranscodeTimeoutSeconds(long transcodeTimeoutSeconds) {
        this.transcodeTimeoutSeconds = transcodeTimeoutSeconds;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }
}
