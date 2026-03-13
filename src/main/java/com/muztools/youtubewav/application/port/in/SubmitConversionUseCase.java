package com.muztools.youtubewav.application.port.in;

import java.util.UUID;

public interface SubmitConversionUseCase {

    UUID submit(String youtubeUrl);
}
