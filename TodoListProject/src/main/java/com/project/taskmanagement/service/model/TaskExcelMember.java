package com.project.taskmanagement.service.model;

import java.util.UUID;

public record TaskExcelMember(

        UUID userId,

        String username,

        String email,

        String projectRole

) {
}