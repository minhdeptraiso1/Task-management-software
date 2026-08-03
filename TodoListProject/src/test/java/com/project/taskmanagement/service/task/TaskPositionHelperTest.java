package com.project.taskmanagement.service.task;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskPositionHelperTest {

    private final TaskPositionHelper helper = new TaskPositionHelper();

    @Test
    void clampPositionKeepsRequestedPositionInsideColumn() {
        assertThat(helper.clampPosition(3L, 5)).isEqualTo(3);
    }

    @Test
    void clampPositionUsesColumnBoundsAndAppendsNullPosition() {
        assertThat(helper.clampPosition(0L, 5)).isEqualTo(1);
        assertThat(helper.clampPosition(10L, 5)).isEqualTo(5);
        assertThat(helper.clampPosition(null, 5)).isEqualTo(5);
        assertThat(helper.clampPosition(null, 0)).isEqualTo(1);
    }
}
