package com.project.taskmanagement.service.cache;

import java.util.UUID;

public interface CacheEvictService {

    void evictProjectWorkspace(UUID projectId);

    void evictProjectMembers(UUID projectId);

    void evictSprintWorkspace(UUID projectId, UUID sprintId);

    void evictBacklogWorkspace(UUID projectId, UUID sprintId);

    void evictTaskWorkspace(
            UUID projectId,
            UUID sprintId,
            UUID taskId,
            UUID assigneeUserId
    );

    void evictTaskComments(UUID projectId, UUID taskId);

    void evictTaskTimeLogs(
            UUID projectId,
            UUID sprintId,
            UUID taskId,
            UUID userId
    );

    void evictNotificationWorkspace(UUID userId);

    void evictSearchWorkspace(UUID projectId);
}
