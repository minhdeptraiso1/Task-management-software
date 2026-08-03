package com.project.taskmanagement.health;

import com.project.taskmanagement.config.FileSecurityProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component("fileStorageHealthIndicator")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class FileStorageHealthIndicator implements HealthIndicator {

    FileSecurityProperties fileSecurityProperties;

    @Override
    public Health health() {
        Path uploadPath = Path.of(
                        fileSecurityProperties.rootPathOrDefault()
                )
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(uploadPath);

            boolean directory = Files.isDirectory(uploadPath);
            boolean readable = Files.isReadable(uploadPath);
            boolean writable = Files.isWritable(uploadPath);

            if (!directory || !readable || !writable) {
                return Health.down()
                        .withDetail("path", uploadPath.toString())
                        .withDetail("directory", directory)
                        .withDetail("readable", readable)
                        .withDetail("writable", writable)
                        .build();
            }

            return Health.up()
                    .withDetail("path", uploadPath.toString())
                    .withDetail("readable", true)
                    .withDetail("writable", true)
                    .build();
        } catch (Exception exception) {
            return Health.down(exception)
                    .withDetail("path", uploadPath.toString())
                    .build();
        }
    }
}
