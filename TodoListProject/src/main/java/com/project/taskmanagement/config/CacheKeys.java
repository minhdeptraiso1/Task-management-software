package com.project.taskmanagement.config;

import java.util.UUID;

public final class CacheKeys {

    private CacheKeys() {
    }

    public static String project(UUID projectId) {
        return required(projectId, "projectId");
    }

    public static String projectMembers(UUID projectId) {
        return project(projectId);
    }

    public static String projectDashboard(UUID projectId, String username) {
        return userScoped(username, project(projectId));
    }

    public static String currentSprint(UUID projectId) {
        return project(projectId);
    }

    public static String sprint(UUID projectId, UUID sprintId) {
        return project(projectId) + ":" + required(sprintId, "sprintId");
    }

    public static String sprintKanban(
            UUID projectId,
            UUID sprintId,
            String username
    ) {
        return userScoped(username, sprint(projectId, sprintId));
    }

    public static String task(UUID projectId, UUID taskId, String username) {
        return userScoped(
                username,
                project(projectId) + ":" + required(taskId, "taskId")
        );
    }

    public static String taskTimeSummary(
            UUID projectId,
            UUID taskId,
            String username
    ) {
        return task(projectId, taskId, username);
    }

    public static String taskComments(
            UUID projectId,
            UUID taskId,
            int page,
            int size,
            String username
    ) {
        return task(projectId, taskId, username)
                + ":" + page
                + ":" + size;
    }

    public static String myDashboard(String username) {
        return required(username, "username");
    }

    public static String unreadNotificationCount(String username) {
        return required(username, "username");
    }

    private static String userScoped(String username, String key) {
        return required(username, "username") + ":" + key;
    }

    private static String required(Object value, String fieldName) {
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.toString();
    }
}
