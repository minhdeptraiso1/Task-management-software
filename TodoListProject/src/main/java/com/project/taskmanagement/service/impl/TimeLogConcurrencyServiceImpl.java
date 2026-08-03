package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.repository.DatabaseLockRepository;
import com.project.taskmanagement.service.TimeLogConcurrencyService;
import com.project.taskmanagement.service.timelog.TimeLogLockKeyGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TimeLogConcurrencyServiceImpl
        implements TimeLogConcurrencyService {

    DatabaseLockRepository databaseLockRepository;
    TimeLogLockKeyGenerator timeLogLockKeyGenerator;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void lockUserWorkDate(
            UUID userId,
            LocalDate workDate
    ) {
        long lockKey = timeLogLockKeyGenerator.generate(
                userId,
                workDate
        );
        databaseLockRepository.acquireTransactionLock(lockKey);
    }
}
