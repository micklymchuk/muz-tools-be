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

    @Min(30)
    @Max(3600)
    private long downloadTimeoutSeconds = 600;

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

    public long getDownloadTimeoutSeconds() {
        return downloadTimeoutSeconds;
    }

    public void setDownloadTimeoutSeconds(long downloadTimeoutSeconds) {
        this.downloadTimeoutSeconds = downloadTimeoutSeconds;
    }

}
