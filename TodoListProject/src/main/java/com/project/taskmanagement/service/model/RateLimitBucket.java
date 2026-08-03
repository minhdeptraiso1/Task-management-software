package com.project.taskmanagement.service.model;

import java.time.Instant;

public class RateLimitBucket {

    private int count;

    private Instant windowStart;

    public RateLimitBucket(
            Instant windowStart
    ) {
        this.windowStart = windowStart;
        this.count = 0;
    }

    public int getCount() {
        return count;
    }

    public void increment() {
        this.count++;
    }

    public void reset(
            Instant windowStart
    ) {
        this.windowStart = windowStart;
        this.count = 0;
    }

    public Instant getWindowStart() {
        return windowStart;
    }
}
