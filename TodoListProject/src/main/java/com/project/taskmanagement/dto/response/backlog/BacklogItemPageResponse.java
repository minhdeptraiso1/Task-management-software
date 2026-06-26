package com.project.taskmanagement.dto.response.backlog;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record BacklogItemPageResponse(

        List<BacklogItemResponse> content,

        long totalElements,

        int totalPages,

        int number,

        int size,

        int numberOfElements,

        boolean first,

        boolean last,

        boolean empty

) {

    public static BacklogItemPageResponse from(
            Page<BacklogItemResponse> page
    ) {
        return new BacklogItemPageResponse(
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