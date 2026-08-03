package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.repository.DatabaseLockRepository;
import com.project.taskmanagement.service.timelog.TimeLogLockKeyGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimeLogConcurrencyServiceImplTest {

    @Test
    void acquiresTransactionLockUsingGeneratedScopeKey() {
        DatabaseLockRepository repository =
                mock(DatabaseLockRepository.class);
        TimeLogLockKeyGenerator generator =
                mock(TimeLogLockKeyGenerator.class);
        TimeLogConcurrencyServiceImpl service =
                new TimeLogConcurrencyServiceImpl(repository, generator);
        UUID userId = UUID.randomUUID();
        LocalDate workDate = LocalDate.of(2026, 8, 3);
        long lockKey = 123456L;

        when(generator.generate(userId, workDate))
                .thenReturn(lockKey);

        service.lockUserWorkDate(userId, workDate);

        verify(repository).acquireTransactionLock(lockKey);
    }
}
