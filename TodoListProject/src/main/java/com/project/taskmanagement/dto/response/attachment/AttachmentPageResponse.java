package com.project.taskmanagement.dto.response.attachment;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record AttachmentPageResponse(
        List<AttachmentResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean empty
) {

    public static AttachmentPageResponse from(Page<AttachmentResponse> page) {
        return new AttachmentPageResponse(
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
