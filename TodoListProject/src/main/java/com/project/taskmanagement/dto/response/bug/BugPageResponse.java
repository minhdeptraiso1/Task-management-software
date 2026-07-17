package com.project.taskmanagement.dto.response.bug;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record BugPageResponse(
        List<BugResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean empty
) {
    public static BugPageResponse from(
            Page<BugResponse> page
    ) {
        return new BugPageResponse(
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
