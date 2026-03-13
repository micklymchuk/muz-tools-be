package com.muztools.youtubewav.infrastructure.persistence;

import com.muztools.youtubewav.application.port.out.ConversionJobRepository;
import com.muztools.youtubewav.domain.ConversionJob;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryConversionJobRepository implements ConversionJobRepository {

    private final ConcurrentMap<UUID, ConversionJob> jobs = new ConcurrentHashMap<>();

    @Override
    public ConversionJob save(ConversionJob job) {
        jobs.put(job.getId(), job);
        return job;
    }

    @Override
    public Optional<ConversionJob> findById(UUID jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }
}
