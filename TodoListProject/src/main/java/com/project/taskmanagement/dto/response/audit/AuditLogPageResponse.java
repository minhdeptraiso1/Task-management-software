package com.project.taskmanagement.dto.response.audit;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record AuditLogPageResponse(
        List<AuditLogResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean empty
) {
    public static AuditLogPageResponse from(Page<AuditLogResponse> page) {
        return new AuditLogPageResponse(
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
