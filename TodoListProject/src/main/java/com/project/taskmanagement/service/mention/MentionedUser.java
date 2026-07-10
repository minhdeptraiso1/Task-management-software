package com.project.taskmanagement.service.mention;

import java.util.UUID;

public record MentionedUser(
        UUID userId,
        String username
) {
}
