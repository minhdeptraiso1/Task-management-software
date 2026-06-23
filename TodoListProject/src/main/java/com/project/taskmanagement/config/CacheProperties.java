package com.project.taskmanagement.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    private Duration defaultTtl = Duration.ofMinutes(10);

    private Map<String, Duration> ttl = new HashMap<>();
}