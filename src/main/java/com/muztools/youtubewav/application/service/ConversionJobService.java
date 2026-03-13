package com.muztools.youtubewav.application.service;

import com.muztools.common.exception.BadRequestException;
import com.muztools.common.exception.ConflictException;
import com.muztools.common.exception.NotFoundException;
import com.muztools.youtubewav.application.port.in.DownloadConvertedFileUseCase;
import com.muztools.youtubewav.application.port.in.GetConversionJobUseCase;
import com.muztools.youtubewav.application.port.in.GetConverterCapabilitiesUseCase;
import com.muztools.youtubewav.application.port.in.SubmitConversionUseCase;
import com.muztools.youtubewav.application.port.out.ConversionJobRepository;
import com.muztools.youtubewav.application.port.out.ConversionStorage;
import com.muztools.youtubewav.domain.ConversionJob;
import com.muztools.youtubewav.domain.ConversionStatus;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.UUID;

@Service
public class ConversionJobService implements SubmitConversionUseCase, GetConversionJobUseCase,
        DownloadConvertedFileUseCase, GetConverterCapabilitiesUseCase {

    private final ConversionJobRepository conversionJobRepository;
    private final ConversionStorage conversionStorage;
    private final ConverterCapabilitiesService converterCapabilitiesService;
    private final ConversionProcessor conversionProcessor;

    public ConversionJobService(ConversionJobRepository conversionJobRepository, ConversionStorage conversionStorage,
                                ConverterCapabilitiesService converterCapabilitiesService,
                                ConversionProcessor conversionProcessor) {
        this.conversionJobRepository = conversionJobRepository;
        this.conversionStorage = conversionStorage;
        this.converterCapabilitiesService = converterCapabilitiesService;
        this.conversionProcessor = conversionProcessor;
    }

    @Override
    public UUID submit(String youtubeUrl) {
        validateYouTubeUrl(youtubeUrl);

        ConversionJob job = ConversionJob.create(youtubeUrl);
        conversionJobRepository.save(job);
        conversionProcessor.processAsync(job.getId());
        return job.getId();
    }

    @Override
    public ConversionJob getById(UUID jobId) {
        return conversionJobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Conversion job not found: " + jobId));
    }

    @Override
    public Resource download(UUID jobId) {
        ConversionJob job = getById(jobId);
        if (job.getStatus() != ConversionStatus.COMPLETED || job.getOutputFilename() == null) {
            throw new ConflictException("Conversion file is not ready for job: " + jobId);
        }
        return conversionStorage.loadAsResource(jobId, job.getOutputFilename());
    }

    @Override
    public ConverterCapabilities getCapabilities() {
        return converterCapabilitiesService.getCapabilities();
    }

    private void validateYouTubeUrl(String youtubeUrl) {
        try {
            URI uri = new URI(youtubeUrl);
            String host = uri.getHost();
            if (host == null) {
                throw new BadRequestException("Invalid YouTube URL");
            }

            String normalizedHost = host.toLowerCase(Locale.ROOT);
            if (!normalizedHost.equals("youtube.com")
                    && !normalizedHost.endsWith(".youtube.com")
                    && !normalizedHost.equals("youtu.be")
                    && !normalizedHost.endsWith(".youtu.be")) {
                throw new BadRequestException("Only YouTube URLs are supported");
            }
        } catch (URISyntaxException exception) {
            throw new BadRequestException("Invalid YouTube URL");
        }
    }

}
