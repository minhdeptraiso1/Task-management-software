package com.project.taskmanagement.dto.response.core;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseSeverTest {

    @Test
    void ok_shouldCreateConsistentSuccessResponse() {
        ApiResponseSever<String> response = ApiResponseSever.ok("payload");

        assertThat(response.code()).isEqualTo(1000);
        assertThat(response.message()).isEqualTo("Thành công");
        assertThat(response.data()).isEqualTo("payload");
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.path()).isNull();
    }

    @Test
    void withPath_shouldPreserveResponseAndAddRequestPath() {
        ApiResponseSever<Void> response = ApiResponseSever
                .<Void>of(404002, "Không tìm thấy tài nguyên", null)
                .withPath("/projects/missing");

        assertThat(response.code()).isEqualTo(404002);
        assertThat(response.message()).isEqualTo("Không tìm thấy tài nguyên");
        assertThat(response.data()).isNull();
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.path()).isEqualTo("/projects/missing");
    }
}
