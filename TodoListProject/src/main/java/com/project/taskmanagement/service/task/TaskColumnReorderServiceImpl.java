package com.project.taskmanagement.service.task;

import com.project.taskmanagement.entity.Task;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskColumnReorderServiceImpl
        implements TaskColumnReorderService {

    TaskPositionHelper taskPositionHelper;

    private static final Comparator<Task> COLUMN_ORDER =
            Comparator.comparing(
                            Task::getPosition,
                            Comparator.nullsLast(Long::compareTo)
                    )
                    .thenComparing(
                            Task::getCreatedAt,
                            Comparator.nullsLast(Comparator.naturalOrder())
                    )
                    .thenComparing(Task::getId);

    @Override
    public void reorderColumn(
            List<Task> columnTasks,
            UUID movingTaskId,
            Long targetPosition
    ) {
        List<Task> sortedTasks = new ArrayList<>(columnTasks);
        sortedTasks.sort(COLUMN_ORDER);

        Task movingTask = sortedTasks.stream()
                .filter(task -> task.getId().equals(movingTaskId))
                .findFirst()
                .orElse(null);

        if (movingTask == null) {
            return;
        }

        sortedTasks.remove(movingTask);

        int safePosition = taskPositionHelper.clampPosition(
                targetPosition,
                sortedTasks.size() + 1
        );

        sortedTasks.add(safePosition - 1, movingTask);
        assignContinuousPositions(sortedTasks);
    }

    @Override
    public void normalizeColumn(List<Task> columnTasks) {
        List<Task> sortedTasks = new ArrayList<>(columnTasks);
        sortedTasks.sort(COLUMN_ORDER);
        assignContinuousPositions(sortedTasks);
    }

    private void assignContinuousPositions(List<Task> tasks) {
        long position = 1L;
        for (Task task : tasks) {
            task.setPosition(position++);
        }
    }
}
