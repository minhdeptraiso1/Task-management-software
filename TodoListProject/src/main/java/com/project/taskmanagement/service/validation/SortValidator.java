package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class SortValidator {

    private SortValidator() {
    }

    public static void validate(
            Pageable pageable,
            Set<String> allowedFields
    ) {
        if (pageable == null
                || pageable.getSort().isUnsorted()) {
            return;
        }

        for (Sort.Order order : pageable.getSort()) {
            if (!allowedFields.contains(order.getProperty())) {
                throw new BusinessException(
                        ErrorCode.FILTER_SORT_FIELD_INVALID
                );
            }
        }
    }
}
