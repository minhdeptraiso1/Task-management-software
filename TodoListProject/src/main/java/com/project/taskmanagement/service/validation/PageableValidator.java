package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public final class PageableValidator {

    private static final int MAX_PAGE_SIZE = 100;

    private PageableValidator() {
    }

    public static void validate(Pageable pageable, Set<String> allowedSortFields) {
        if (pageable == null) {
            return;
        }

        validate(pageable);

        if (allowedSortFields == null || allowedSortFields.isEmpty()) {
            return;
        }

        pageable.getSort().forEach(order -> {
            if (!allowedSortFields.contains(order.getProperty())) {
                throw new BusinessException(ErrorCode.FILTER_SORT_FIELD_INVALID);
            }
        });
    }

    public static void validate(Pageable pageable) {
        if (pageable == null) {
            return;
        }

        if (pageable.getPageSize() > MAX_PAGE_SIZE) {
            throw new BusinessException(ErrorCode.FILTER_PAGE_SIZE_INVALID);
        }

        if (pageable.getPageSize() < 1) {
            throw new BusinessException(ErrorCode.FILTER_PAGE_SIZE_INVALID);
        }
    }
}
