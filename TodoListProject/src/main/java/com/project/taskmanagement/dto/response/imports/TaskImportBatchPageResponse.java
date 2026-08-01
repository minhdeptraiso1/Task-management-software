package com.project.taskmanagement.dto.response.imports;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record TaskImportBatchPageResponse(

        List<TaskImportBatchResponse> content,

        long totalElements,

        int totalPages,

        int number,

        int size,

        int numberOfElements,

        boolean first,

        boolean last,

        boolean empty
) {

    public static TaskImportBatchPageResponse from(
            Page<TaskImportBatchResponse> page
    ) {
        return new TaskImportBatchPageResponse(
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
