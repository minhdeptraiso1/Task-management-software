package com.project.taskmanagement.dto.response.sprint;

import java.time.LocalDate;
import java.util.UUID;

public record SprintRetroActionItemResponse(

        String content,

        UUID assigneeUserId,

        String assigneeUsername,

        String assigneeEmail,

        LocalDate dueDate,

        boolean done

) {
}
