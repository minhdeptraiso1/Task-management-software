package com.project.taskmanagement.dto.request.sprint;

import java.util.List;

public record UpdateSprintRetrospectiveRequest(

        String wentWell,

        String wentWrong,

        String improvement,

        List<SprintRetroActionItemRequest> actionItems,

        String note

) {
}
