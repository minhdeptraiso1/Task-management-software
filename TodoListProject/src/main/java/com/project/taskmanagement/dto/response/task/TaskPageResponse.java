package com.project.taskmanagement.dto.response.task;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record TaskPageResponse(

        List<TaskResponse> content,

        long totalElements,

        int totalPages,

        int number,

        int size,

        int numberOfElements,

        boolean first,

        boolean last,

        boolean empty

) {

    public static TaskPageResponse from(
            Page<TaskResponse> page
    ) {
        return new TaskPageResponse(
                new ArrayList<>(page.getContent()),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}