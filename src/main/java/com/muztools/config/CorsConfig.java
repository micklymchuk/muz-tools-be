package com.muztools.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;

    public CorsConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var registration = registry.addMapping("/api/**")
                .allowedMethods(corsProperties.getAllowedMethods().toArray(String[]::new))
                .allowedHeaders(corsProperties.getAllowedHeaders().toArray(String[]::new))
                .allowCredentials(corsProperties.isAllowCredentials())
                .maxAge(corsProperties.getMaxAgeSeconds());

        if (!corsProperties.getAllowedOrigins().isEmpty()) {
            registration.allowedOrigins(corsProperties.getAllowedOrigins().toArray(String[]::new));
        }

        if (!corsProperties.getAllowedOriginPatterns().isEmpty()) {
            registration.allowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(String[]::new));
        }
    }
}
