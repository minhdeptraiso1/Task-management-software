package com.project.taskmanagement.dto.response.timesheet;

import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

public record TimesheetPageResponse(

        List<TimesheetEntryResponse> content,

        long totalElements,

        int totalPages,

        int number,

        int size,

        int numberOfElements,

        boolean first,

        boolean last,

        boolean empty

) {

    public static TimesheetPageResponse from(
            Page<TimesheetEntryResponse> page
    ) {
        return new TimesheetPageResponse(
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
