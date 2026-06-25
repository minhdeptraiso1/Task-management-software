package com.project.taskmanagement.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

/**
 * Sinh UUID version 7.
 *
 * Cấu trúc:
 * - 48 bit đầu: Unix timestamp milliseconds.
 * - 4 bit version: 0111.
 * - 12 bit random/counter.
 * - 2 bit variant: 10.
 * - 62 bit random.
 *
 * Generator được đồng bộ để giảm nguy cơ đảo thứ tự
 * khi nhiều UUID được sinh trong cùng một millisecond.
 */
public final class UuidV7Generator {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    private static long lastTimestamp = -1L;
    private static int sequence = 0;

    private UuidV7Generator() {
    }

    public static synchronized UUID generate() {
        long timestamp = Instant.now().toEpochMilli();

        if (timestamp > lastTimestamp) {
            lastTimestamp = timestamp;

            /*
             * UUIDv7 có 12 bit rand_a.
             * Giá trị hợp lệ từ 0 đến 4095.
             */
            sequence = RANDOM.nextInt(1 << 12);

        } else {
            /*
             * Đồng hồ không tăng hoặc đang sinh nhiều UUID
             * trong cùng millisecond.
             */
            timestamp = lastTimestamp;
            sequence = (sequence + 1) & 0x0FFF;

            /*
             * Nếu 12-bit sequence đã quay vòng,
             * đợi millisecond tiếp theo.
             */
            if (sequence == 0) {
                timestamp = waitUntilNextMillis(
                        lastTimestamp
                );

                lastTimestamp = timestamp;
                sequence = RANDOM.nextInt(1 << 12);
            }
        }

        /*
         * Most significant bits:
         *
         * 48 bit timestamp
         * 4 bit version = 7
         * 12 bit rand_a/sequence
         */
        long mostSignificantBits =
                ((timestamp & 0xFFFFFFFFFFFFL) << 16)
                        | 0x7000L
                        | (sequence & 0x0FFFL);

        /*
         * Least significant bits:
         *
         * Xóa 2 bit cao rồi đặt variant RFC thành 10.
         */
        long leastSignificantBits =
                (RANDOM.nextLong()
                        & 0x3FFFFFFFFFFFFFFFL)
                        | 0x8000000000000000L;

        return new UUID(
                mostSignificantBits,
                leastSignificantBits
        );
    }

    private static long waitUntilNextMillis(
            long previousTimestamp
    ) {
        long currentTimestamp;

        do {
            currentTimestamp =
                    Instant.now().toEpochMilli();
        } while (currentTimestamp <= previousTimestamp);

        return currentTimestamp;
    }
}