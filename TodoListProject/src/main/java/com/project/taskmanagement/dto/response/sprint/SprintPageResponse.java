package com.project.taskmanagement.dto.response.sprint;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record SprintPageResponse(

        List<SprintResponse> content,

        long totalElements,

        int totalPages,

        int number,

        int size,

        int numberOfElements,

        boolean first,

        boolean last,

        boolean empty

) {

    public static SprintPageResponse from(
            Page<SprintResponse> page
    ) {
        return new SprintPageResponse(
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