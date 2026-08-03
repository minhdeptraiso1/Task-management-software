package com.project.taskmanagement.service;

import java.time.LocalDate;
import java.util.UUID;

public interface TimeLogConcurrencyService {

    void lockUserWorkDate(
            UUID userId,
            LocalDate workDate
    );
}
