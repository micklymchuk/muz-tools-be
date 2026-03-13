package com.muztools.youtubewav.web;

import com.muztools.common.api.GlobalExceptionHandler;
import com.muztools.common.exception.NotFoundException;
import com.muztools.youtubewav.application.port.in.DownloadConvertedFileUseCase;
import com.muztools.youtubewav.application.port.in.GetConversionJobUseCase;
import com.muztools.youtubewav.application.port.in.GetConverterCapabilitiesUseCase;
import com.muztools.youtubewav.application.port.in.SubmitConversionUseCase;
import com.muztools.youtubewav.application.service.ConverterCapabilities;
import com.muztools.youtubewav.domain.ConversionJob;
import com.muztools.youtubewav.domain.ConversionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class YouTubeWavControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SubmitConversionUseCase submitConversionUseCase;

    @Mock
    private GetConversionJobUseCase getConversionJobUseCase;

    @Mock
    private DownloadConvertedFileUseCase downloadConvertedFileUseCase;

    @Mock
    private GetConverterCapabilitiesUseCase getConverterCapabilitiesUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new YouTubeWavController(
                        submitConversionUseCase,
                        getConversionJobUseCase,
                        downloadConvertedFileUseCase,
                        getConverterCapabilitiesUseCase
                ))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createJobShouldReturnAcceptedResponse() throws Exception {
        UUID jobId = UUID.randomUUID();
        ConversionJob job = new ConversionJob(
                jobId,
                "https://www.youtube.com/watch?v=test123",
                Instant.now(),
                ConversionStatus.PENDING,
                Instant.now(),
                null,
                null
        );
        when(submitConversionUseCase.submit("https://www.youtube.com/watch?v=test123")).thenReturn(jobId);
        when(getConversionJobUseCase.getById(jobId)).thenReturn(job);

        mockMvc.perform(post("/api/v1/tools/youtube-to-wav/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"youtubeUrl\":\"https://www.youtube.com/watch?v=test123\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(jobId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.downloadUrl").value("http://localhost/api/v1/tools/youtube-to-wav/jobs/" + jobId + "/file"));
    }

    @Test
    void getCapabilitiesShouldExposeToolAvailability() throws Exception {
        when(getConverterCapabilitiesUseCase.getCapabilities()).thenReturn(new ConverterCapabilities(true, false));

        mockMvc.perform(get("/api/v1/tools/youtube-to-wav/capabilities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ready").value(false))
                .andExpect(jsonPath("$.ytDlpAvailable").value(true))
                .andExpect(jsonPath("$.ffmpegAvailable").value(false));
    }

    @Test
    void downloadShouldReturnAudioResource() throws Exception {
        UUID jobId = UUID.randomUUID();
        ConversionJob job = new ConversionJob(
                jobId,
                "https://youtu.be/test123",
                Instant.now(),
                ConversionStatus.COMPLETED,
                Instant.now(),
                null,
                "demo.wav"
        );
        when(getConversionJobUseCase.getById(jobId)).thenReturn(job);
        when(downloadConvertedFileUseCase.download(jobId)).thenReturn(new ByteArrayResource(new byte[]{1, 2, 3}));

        mockMvc.perform(get("/api/v1/tools/youtube-to-wav/jobs/{jobId}/file", jobId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"demo.wav\""))
                .andExpect(content().contentType("audio/wav"));
    }

    @Test
    void getJobShouldReturnNotFoundWhenMissing() throws Exception {
        UUID jobId = UUID.randomUUID();
        when(getConversionJobUseCase.getById(jobId)).thenThrow(new NotFoundException("Conversion job not found: " + jobId));

        mockMvc.perform(get("/api/v1/tools/youtube-to-wav/jobs/{jobId}", jobId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Conversion job not found: " + jobId));
    }
}
