package com.project.taskmanagement.service.validation;

import java.util.Locale;

public final class TextInputSanitizer {

    private TextInputSanitizer() {
    }

    public static String trim(
            String value
    ) {
        return value == null
                ? null
                : value.trim();
    }

    public static String trimToNull(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed =
                value.trim();

        return trimmed.isBlank()
                ? null
                : trimmed;
    }

    public static String normalizeKeyword(
            String keyword
    ) {
        return trimToNull(keyword);
    }

    public static String normalizeEmail(
            String email
    ) {
        String normalized =
                trimToNull(email);

        return normalized == null
                ? null
                : normalized.toLowerCase(Locale.ROOT);
    }
}
