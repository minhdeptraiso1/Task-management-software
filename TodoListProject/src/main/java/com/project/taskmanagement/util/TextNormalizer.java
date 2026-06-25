package com.project.taskmanagement.util;

import java.util.Locale;

public final class TextNormalizer {

    private TextNormalizer() {
    }

    public static String trim(String value) {
        return value == null
                ? null
                : value.trim();
    }

    public static String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public static String uppercase(String value) {
        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    public static String lowercase(String value) {
        if (value == null) {
            return null;
        }

        return value
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}

