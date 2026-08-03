package com.project.taskmanagement.service.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.enums.ProjectActivityAction;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectActivityMessageResolverTest {

    private final ProjectActivityMessageResolver resolver =
            new ProjectActivityMessageResolver(new ObjectMapper());

    @Test
    void resolvesReadableTaskCreatedMessage() {
        ProjectActivityLog activity = ProjectActivityLog.builder()
                .action(ProjectActivityAction.TASK_CREATED)
                .newValueJson("{\"title\":\"Thiết kế API đăng nhập\"}")
                .build();

        assertThat(resolver.resolve(activity, "Nguyễn Văn A"))
                .contains("Nguyễn Văn A")
                .contains("Task")
                .contains("Thiết kế API đăng nhập");
    }

    @Test
    void resolvesTaskStatusChangeWithBothStatuses() {
        ProjectActivityLog activity = ProjectActivityLog.builder()
                .action(ProjectActivityAction.TASK_STATUS_CHANGED)
                .oldValueJson("{\"status\":\"TODO\"}")
                .newValueJson("{\"status\":\"IN_PROGRESS\"}")
                .build();

        assertThat(resolver.resolve(activity, "Dev Minh"))
                .contains("Dev Minh")
                .contains("TODO")
                .contains("IN_PROGRESS");
    }

    @Test
    void toleratesMalformedJsonWithoutExposingRawPayload() {
        ProjectActivityLog activity = ProjectActivityLog.builder()
                .action(ProjectActivityAction.TASK_UPDATED)
                .newValueJson("{not-json password=secret}")
                .build();

        assertThat(resolver.resolve(activity, null))
                .contains("Task")
                .doesNotContain("password")
                .doesNotContain("secret");
    }
}
