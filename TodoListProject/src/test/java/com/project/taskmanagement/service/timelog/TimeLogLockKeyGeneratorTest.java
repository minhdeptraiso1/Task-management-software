package com.project.taskmanagement.service.timelog;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TimeLogLockKeyGeneratorTest {

    private final TimeLogLockKeyGenerator generator =
            new TimeLogLockKeyGenerator();

    @Test
    void generatesStableKeyForSameUserAndDate() {
        UUID userId = UUID.randomUUID();
        LocalDate workDate = LocalDate.of(2026, 8, 3);

        assertThat(generator.generate(userId, workDate))
                .isEqualTo(generator.generate(userId, workDate));
    }

    @Test
    void changesKeyWhenUserOrDateChanges() {
        UUID firstUser = UUID.randomUUID();
        UUID secondUser = UUID.randomUUID();
        LocalDate firstDate = LocalDate.of(2026, 8, 3);
        LocalDate secondDate = firstDate.plusDays(1);

        long baseKey = generator.generate(firstUser, firstDate);

        assertThat(generator.generate(secondUser, firstDate))
                .isNotEqualTo(baseKey);
        assertThat(generator.generate(firstUser, secondDate))
                .isNotEqualTo(baseKey);
    }
}
