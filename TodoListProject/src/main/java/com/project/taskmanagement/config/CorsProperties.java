package com.project.taskmanagement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        String allowedOrigins,
        Boolean allowCredentials
) {

    public List<String> allowedOriginList() {
        List<String> origins = Arrays.stream(
                        (allowedOrigins == null ? "" : allowedOrigins).split(",")
                )
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .distinct()
                .toList();

        if (origins.isEmpty()) {
            return List.of("http://localhost:5173");
        }

        if (origins.contains("*")) {
            throw new IllegalStateException(
                    "app.cors.allowed-origins must contain explicit frontend origins"
            );
        }

        return origins;
    }

    public String[] allowedOriginArray() {
        return allowedOriginList().toArray(String[]::new);
    }

    public boolean allowCredentialsOrDefault() {
        return Boolean.TRUE.equals(allowCredentials);
    }
}
