package com.project.taskmanagement.service.timelog;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;
import java.util.zip.CRC32;

@Component
public class TimeLogLockKeyGenerator {

    public long generate(
            UUID userId,
            LocalDate workDate
    ) {
        String rawKey = userId + ":" + workDate;
        CRC32 crc32 = new CRC32();
        crc32.update(rawKey.getBytes(StandardCharsets.UTF_8));
        return crc32.getValue();
    }
}
