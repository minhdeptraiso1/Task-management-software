package com.project.taskmanagement.service;

import com.project.taskmanagement.enums.RateLimitAction;

public interface RateLimitService {

    void check(
            RateLimitAction action,
            String key
    );
}
