package com.project.taskmanagement.exception;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorCodeTest {

    @Test
    void numericCodesMustBeUnique() {
        Map<Integer, Long> occurrences = Arrays.stream(ErrorCode.values())
                .collect(Collectors.groupingBy(
                        ErrorCode::code,
                        Collectors.counting()
                ));

        Map<Integer, Long> duplicates = occurrences.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));

        assertTrue(
                duplicates.isEmpty(),
                () -> "Duplicate ErrorCode values: " + duplicates
        );
    }

    @Test
    void everyErrorCodeMustHaveStatusAndReadableMessage() {
        assertThat(ErrorCode.values()).allSatisfy(errorCode -> {
            assertThat(errorCode.status())
                    .as("HTTP status của %s", errorCode.name())
                    .isNotNull();
            assertThat(errorCode.message())
                    .as("Message của %s", errorCode.name())
                    .isNotBlank()
                    .doesNotContain("Ã", "Â", "Ä", "á»");
        });
    }

    @Test
    void numericCodeFamilyMustMatchHttpStatus() {
        Map<Integer, Set<Integer>> expectedStatusByFamily = Map.of(
                400, Set.of(400),
                401, Set.of(401),
                403, Set.of(403),
                404, Set.of(404),
                405, Set.of(405),
                409, Set.of(409),
                415, Set.of(415),
                422, Set.of(422),
                429, Set.of(429),
                500, Set.of(500)
        );

        assertThat(ErrorCode.values()).allSatisfy(errorCode -> {
            int family = errorCode.code() / 1000;
            assertThat(expectedStatusByFamily)
                    .as("Nhóm numeric code của %s", errorCode.name())
                    .containsKey(family);
            assertThat(expectedStatusByFamily.get(family))
                    .as("HTTP status không khớp numeric code của %s", errorCode.name())
                    .contains(errorCode.status().value());
        });
    }
}
