package com.muztools.youtubewav.web;

import com.muztools.youtubewav.application.port.in.DownloadConvertedFileUseCase;
import com.muztools.youtubewav.application.port.in.GetConversionJobUseCase;
import com.muztools.youtubewav.application.port.in.GetConverterCapabilitiesUseCase;
import com.muztools.youtubewav.application.port.in.SubmitConversionUseCase;
import com.muztools.youtubewav.domain.ConversionJob;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tools/youtube-to-wav")
public class YouTubeWavController {

    private final SubmitConversionUseCase submitConversionUseCase;
    private final GetConversionJobUseCase getConversionJobUseCase;
    private final DownloadConvertedFileUseCase downloadConvertedFileUseCase;
    private final GetConverterCapabilitiesUseCase getConverterCapabilitiesUseCase;

    public YouTubeWavController(SubmitConversionUseCase submitConversionUseCase,
                                GetConversionJobUseCase getConversionJobUseCase,
                                DownloadConvertedFileUseCase downloadConvertedFileUseCase,
                                GetConverterCapabilitiesUseCase getConverterCapabilitiesUseCase) {
        this.submitConversionUseCase = submitConversionUseCase;
        this.getConversionJobUseCase = getConversionJobUseCase;
        this.downloadConvertedFileUseCase = downloadConvertedFileUseCase;
        this.getConverterCapabilitiesUseCase = getConverterCapabilitiesUseCase;
    }

    @PostMapping("/jobs")
    public ResponseEntity<ConversionJobResponse> createJob(@Valid @RequestBody CreateConversionJobRequest request) {
        UUID jobId = submitConversionUseCase.submit(request.youtubeUrl());
        ConversionJob job = getConversionJobUseCase.getById(jobId);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ConversionJobResponse.from(job, buildDownloadUrl(job)));
    }

    @GetMapping("/jobs/{jobId}")
    public ConversionJobResponse getJob(@PathVariable UUID jobId) {
        ConversionJob job = getConversionJobUseCase.getById(jobId);
        return ConversionJobResponse.from(job, buildDownloadUrl(job));
    }

    @GetMapping("/jobs/{jobId}/file")
    public ResponseEntity<Resource> download(@PathVariable UUID jobId) {
        ConversionJob job = getConversionJobUseCase.getById(jobId);
        Resource resource = downloadConvertedFileUseCase.download(jobId);
        String filename = job.getOutputFilename() == null ? jobId + ".wav" : job.getOutputFilename();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/wav"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .body(resource);
    }

    @GetMapping("/capabilities")
    public ConverterCapabilitiesResponse getCapabilities() {
        return ConverterCapabilitiesResponse.from(getConverterCapabilitiesUseCase.getCapabilities());
    }

    private String buildDownloadUrl(ConversionJob job) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/tools/youtube-to-wav/jobs/{jobId}/file")
                .buildAndExpand(job.getId())
                .toUriString();
    }
}
