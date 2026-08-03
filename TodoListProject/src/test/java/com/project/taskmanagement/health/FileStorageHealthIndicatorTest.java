package com.project.taskmanagement.health;

import com.project.taskmanagement.config.FileSecurityProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.actuate.health.Status;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class FileStorageHealthIndicatorTest {

    @TempDir
    Path tempDirectory;

    @Test
    void healthCreatesMissingUploadDirectoryAndReturnsUp() {
        Path uploadDirectory = tempDirectory.resolve("nested/uploads");
        FileStorageHealthIndicator indicator = indicator(uploadDirectory);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(Files.isDirectory(uploadDirectory)).isTrue();
        assertThat(health.getDetails())
                .containsEntry("readable", true)
                .containsEntry("writable", true);
    }

    @Test
    void healthReturnsDownWhenConfiguredPathIsAFile() throws Exception {
        Path invalidUploadPath = Files.createFile(
                tempDirectory.resolve("not-a-directory")
        );
        FileStorageHealthIndicator indicator = indicator(invalidUploadPath);

        var health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }

    private FileStorageHealthIndicator indicator(Path rootPath) {
        return new FileStorageHealthIndicator(
                new FileSecurityProperties(
                        rootPath.toString(),
                        null,
                        null,
                        null,
                        null
                )
        );
    }
}
