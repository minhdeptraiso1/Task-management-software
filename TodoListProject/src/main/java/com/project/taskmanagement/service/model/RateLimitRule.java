package com.project.taskmanagement.service.model;

import java.time.Duration;

public record RateLimitRule(

        int maxRequests,

        Duration window

) {
}
