package com.project.taskmanagement.dto.response.admin;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record SystemAuditLogPageResponse(

        List<SystemAuditLogResponse> content,

        long totalElements,

        int totalPages,

        int number,

        int size,

        int numberOfElements,

        boolean first,

        boolean last,

        boolean empty
) {

    public static SystemAuditLogPageResponse from(
            Page<SystemAuditLogResponse> page
    ) {
        return new SystemAuditLogPageResponse(
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
