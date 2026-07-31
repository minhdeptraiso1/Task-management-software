package com.project.taskmanagement.service;

import com.project.taskmanagement.dto.response.scheduler.DailyDigestRunResponse;

import java.time.LocalDate;

public interface DailyDigestService {

    DailyDigestRunResponse runDailyDigest(
            LocalDate businessDate
    );
}
