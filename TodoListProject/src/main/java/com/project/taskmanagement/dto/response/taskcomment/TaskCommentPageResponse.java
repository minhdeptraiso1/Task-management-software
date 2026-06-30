package com.project.taskmanagement.dto.response.taskcomment;

import java.util.List;

public record TaskCommentPageResponse(

        List<TaskCommentResponse> content,

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