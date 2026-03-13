package com.muztools.youtubewav.application.port.out;

import com.muztools.youtubewav.domain.DownloadedAudio;

import java.nio.file.Path;

public interface YouTubeAudioDownloader {

    DownloadedAudio download(String url, Path workingDirectory);
}
