package com.muztools.youtubewav.application.service;

import com.muztools.common.exception.BadRequestException;
import com.muztools.common.exception.ConflictException;
import com.muztools.youtubewav.application.port.out.ConversionJobRepository;
import com.muztools.youtubewav.application.port.out.ConversionStorage;
import com.muztools.youtubewav.domain.ConversionJob;
import com.muztools.youtubewav.domain.ConversionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConversionJobServiceTest {

    @Mock
    private ConversionJobRepository conversionJobRepository;

    @Mock
    private ConversionStorage conversionStorage;

    @Mock
    private ConverterCapabilitiesService converterCapabilitiesService;

    @Mock
    private ConversionProcessor conversionProcessor;

    private ConversionJobService conversionJobService;

    @BeforeEach
    void setUp() {
        conversionJobService = new ConversionJobService(
                conversionJobRepository,
                conversionStorage,
                converterCapabilitiesService,
                conversionProcessor
        );
    }

    @Test
    void submitShouldPersistJobAndStartProcessing() {
        ArgumentCaptor<ConversionJob> jobCaptor = ArgumentCaptor.forClass(ConversionJob.class);
        when(conversionJobRepository.save(jobCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        UUID jobId = conversionJobService.submit("https://www.youtube.com/watch?v=test123");

        assertThat(jobId).isEqualTo(jobCaptor.getValue().getId());
        assertThat(jobCaptor.getValue().getStatus()).isEqualTo(ConversionStatus.PENDING);
        verify(conversionProcessor).processAsync(jobId);
    }

    @Test
    void submitShouldRejectNonYoutubeUrl() {
        assertThatThrownBy(() -> conversionJobService.submit("https://example.com/video"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Only YouTube URLs are supported");
    }

    @Test
    void downloadShouldFailWhenJobIsNotCompleted() {
        UUID jobId = UUID.randomUUID();
        ConversionJob job = new ConversionJob(
                jobId,
                "https://youtu.be/test123",
                Instant.now(),
                ConversionStatus.RUNNING,
                Instant.now(),
                null,
                null
        );
        when(conversionJobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> conversionJobService.download(jobId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not ready");
    }

    @Test
    void downloadShouldReturnStoredResourceWhenJobIsCompleted() {
        UUID jobId = UUID.randomUUID();
        ConversionJob job = new ConversionJob(
                jobId,
                "https://youtu.be/test123",
                Instant.now(),
                ConversionStatus.COMPLETED,
                Instant.now(),
                null,
                "track.wav"
        );
        ByteArrayResource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(conversionJobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(conversionStorage.loadAsResource(jobId, "track.wav")).thenReturn(resource);

        assertThat(conversionJobService.download(jobId)).isEqualTo(resource);
    }
}
