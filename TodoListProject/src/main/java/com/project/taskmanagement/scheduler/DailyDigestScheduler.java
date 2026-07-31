package com.project.taskmanagement.scheduler;

import com.project.taskmanagement.dto.response.scheduler.DailyDigestRunResponse;
import com.project.taskmanagement.service.DailyDigestService;
import com.project.taskmanagement.service.scheduler.SchedulerRunLogger;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class DailyDigestScheduler {

    static final String JOB_NAME =
            "DAILY_DIGEST";

    DailyDigestService dailyDigestService;
    SchedulerRunLogger schedulerRunLogger;

    static final ZoneId BUSINESS_ZONE =
            ZoneId.of("Asia/Ho_Chi_Minh");

    @Scheduled(
            cron = "0 0 8 * * *",
            zone = "Asia/Ho_Chi_Minh"
    )
    public void runDailyDigest() {
        Instant startedAt =
                schedulerRunLogger.start(JOB_NAME);

        LocalDate businessDate =
                LocalDate.now(BUSINESS_ZONE);

        try {
            DailyDigestRunResponse result =
                    dailyDigestService.runDailyDigest(
                            businessDate
                    );

            schedulerRunLogger.success(
                    JOB_NAME,
                    startedAt,
                    result.scannedUsers(),
                    result.sentDigests(),
                    result.skippedUsers()
            );
        } catch (Exception exception) {
            schedulerRunLogger.failed(
                    JOB_NAME,
                    startedAt,
                    exception
                );
        }
    }
}
