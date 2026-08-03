package com.project.taskmanagement.service.task;

import org.springframework.stereotype.Component;

@Component
public class TaskPositionHelper {

    public int clampPosition(
            Long requestedPosition,
            int maxPosition
    ) {
        int safeMaximum = Math.max(maxPosition, 1);

        if (requestedPosition == null) {
            return safeMaximum;
        }

        if (requestedPosition < 1L) {
            return 1;
        }

        if (requestedPosition > safeMaximum) {
            return safeMaximum;
        }

        return requestedPosition.intValue();
    }
}
