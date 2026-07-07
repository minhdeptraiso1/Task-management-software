package com.project.taskmanagement.dto.response.sprint;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SprintReminderResponse(

        UUID projectId,

        UUID sprintId,

        String reminderType,

        long targetItemCount,

        long recipientCount,

        List<UUID> recipientUserIds,

        Instant sentAt

) {
}
