package com.muztools.youtubewav.application.port.out;

import com.muztools.youtubewav.domain.ConversionJob;

import java.util.Optional;
import java.util.UUID;

public interface ConversionJobRepository {

    ConversionJob save(ConversionJob job);

    Optional<ConversionJob> findById(UUID jobId);
}
