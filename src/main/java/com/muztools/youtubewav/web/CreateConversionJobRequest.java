package com.muztools.youtubewav.web;

import jakarta.validation.constraints.NotBlank;

public record CreateConversionJobRequest(@NotBlank String youtubeUrl) {
}
