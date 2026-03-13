package com.muztools.youtubewav.application.port.in;

import com.muztools.youtubewav.domain.ConversionJob;

import java.util.UUID;

public interface GetConversionJobUseCase {

    ConversionJob getById(UUID jobId);
}
