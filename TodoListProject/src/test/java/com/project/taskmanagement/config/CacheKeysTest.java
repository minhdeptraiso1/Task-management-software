package com.project.taskmanagement.config;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheKeysTest {

    @Test
    void buildsStableUserScopedTaskKey() {
        UUID projectId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();

        assertEquals(
                "minh:" + projectId + ":" + taskId,
                CacheKeys.task(projectId, taskId, "minh")
        );
    }

    @Test
    void buildsStableSprintKanbanKey() {
        UUID projectId = UUID.randomUUID();
        UUID sprintId = UUID.randomUUID();

        assertEquals(
                "admin:" + projectId + ":" + sprintId,
                CacheKeys.sprintKanban(projectId, sprintId, "admin")
        );
    }

    @Test
    void rejectsMissingIdentityForUserScopedCache() {
        assertThrows(
                IllegalArgumentException.class,
                () -> CacheKeys.unreadNotificationCount(" ")
        );
    }
}
