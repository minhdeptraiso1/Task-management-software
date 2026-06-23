package com.project.taskmanagement.dto.response.user;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO phân trang ổn định cho API và Redis cache.
 * Không cache trực tiếp PageImpl vì lớp này không hỗ trợ deserialize từ JSON.
 */
public record UserPageResponse(
        List<UserResponse> content,
        long totalElements,
        int totalPages,
        int number,
        int size,
        int numberOfElements,
        boolean first,
        boolean last,
        boolean empty
) {
    public static UserPageResponse from(Page<UserResponse> page) {
        return new UserPageResponse(
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
