package com.project.taskmanagement.dto.request.sprint;

import java.time.LocalDate;
import java.util.UUID;

public record SprintRetroActionItemRequest(

        String content,

        UUID assigneeUserId,

        LocalDate dueDate,

        Boolean done

) {
}
