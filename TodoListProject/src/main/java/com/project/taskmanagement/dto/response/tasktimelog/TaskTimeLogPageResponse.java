package com.project.taskmanagement.dto.response.tasktimelog;

import java.util.List;

public record TaskTimeLogPageResponse(

        List<TaskTimeLogResponse> content,

        long totalElements,

        int totalPages,

        int number,

        int size,

        int numberOfElements,

        boolean first,

        boolean last,

        boolean empty

) {
}