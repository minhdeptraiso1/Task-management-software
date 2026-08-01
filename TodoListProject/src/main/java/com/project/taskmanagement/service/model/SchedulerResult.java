package com.project.taskmanagement.service.model;

public record SchedulerResult(

        long processedCount,

        long createdCount,

        long skippedCount
) {

    public static SchedulerResult empty() {
        return new SchedulerResult(
                0,
                0,
                0
        );
    }
}
