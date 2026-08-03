package com.project.taskmanagement.service;

import com.project.taskmanagement.service.impl.NotificationServiceImpl;
import com.project.taskmanagement.service.impl.ProjectActivityServiceImpl;
import com.project.taskmanagement.service.impl.SprintServiceImpl;
import com.project.taskmanagement.service.impl.TaskImportBatchStateServiceImpl;
import com.project.taskmanagement.service.impl.TaskImportWriterServiceImpl;
import com.project.taskmanagement.service.impl.TaskSprintSyncServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionPropagationTest {

    @Test
    void durableImportBatchFailureState_usesRequiresNew() throws Exception {
        assertPropagation(
                TaskImportBatchStateServiceImpl.class,
                "validationFailed",
                Propagation.REQUIRES_NEW
        );
        assertPropagation(
                TaskImportBatchStateServiceImpl.class,
                "failed",
                Propagation.REQUIRES_NEW
        );
    }

    @Test
    void businessWrites_joinCallerTransaction() throws Exception {
        assertPropagation(SprintServiceImpl.class, "cancel", Propagation.REQUIRED);
        assertPropagation(SprintServiceImpl.class, "complete", Propagation.REQUIRED);
        assertPropagation(TaskSprintSyncServiceImpl.class, "detachBacklogItemTasksFromSprint", Propagation.REQUIRED);
        assertPropagation(TaskImportWriterServiceImpl.class, "importAll", Propagation.REQUIRED);
        assertPropagation(ProjectActivityServiceImpl.class, "log", Propagation.REQUIRED);
        assertPropagation(NotificationServiceImpl.class, "create", Propagation.REQUIRED);
    }

    private void assertPropagation(
            Class<?> type,
            String methodName,
            Propagation expected
    ) throws Exception {
        Method method = java.util.Arrays.stream(type.getMethods())
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional)
                .as("@Transactional on %s.%s", type.getSimpleName(), methodName)
                .isNotNull();
        assertThat(transactional.propagation()).isEqualTo(expected);
    }
}
