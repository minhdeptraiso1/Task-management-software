package com.project.taskmanagement.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class DownloadHeaderUtils {

    private DownloadHeaderUtils() {
    }

    public static String attachmentContentDisposition(String fileName) {
        String normalizedFileName = normalizeFileName(fileName);
        String fallbackFileName = asciiFallback(normalizedFileName);
        String encodedFileName = URLEncoder
                .encode(normalizedFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return "attachment; filename=\"" + fallbackFileName + "\"; filename*=UTF-8''" + encodedFileName;
    }

    public static String normalizeFileName(String fileName) {
        String normalized = fileName == null || fileName.isBlank()
                ? "download"
                : fileName.trim();

        normalized = normalized
                .replace("\\", "_")
                .replace("/", "_")
                .replace("\r", "")
                .replace("\n", "");

        String upper = normalized.toUpperCase(Locale.ROOT);
        if (upper.startsWith("=_UTF-8_Q_") && normalized.endsWith("_=")) {
            String encodedWordBody = normalized.substring("=_UTF-8_Q_".length(), normalized.length() - 2);
            normalized = decodeEncodedWordBody(encodedWordBody);
        } else if (upper.startsWith("=?UTF-8?Q?") && normalized.endsWith("?=")) {
            String encodedWordBody = normalized.substring("=?UTF-8?Q?".length(), normalized.length() - 2);
            normalized = decodeEncodedWordBody(encodedWordBody);
        }

        return normalized.isBlank() ? "download" : normalized;
    }

    private static String decodeEncodedWordBody(String value) {
        StringBuilder builder = new StringBuilder();
        byte[] bytes = new byte[value.length()];
        int byteCount = 0;

        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '=' && index + 2 < value.length()) {
                int high = Character.digit(value.charAt(index + 1), 16);
                int low = Character.digit(value.charAt(index + 2), 16);
                if (high >= 0 && low >= 0) {
                    bytes[byteCount++] = (byte) ((high << 4) + low);
                    index += 2;
                    continue;
                }
            }

            flushBytes(builder, bytes, byteCount);
            byteCount = 0;
            builder.append(current);
        }

        flushBytes(builder, bytes, byteCount);
        return builder.toString();
    }

    private static void flushBytes(StringBuilder builder, byte[] bytes, int byteCount) {
        if (byteCount > 0) {
            builder.append(new String(bytes, 0, byteCount, StandardCharsets.UTF_8));
        }
    }

    private static String asciiFallback(String fileName) {
        String fallback = fileName.replaceAll("[^A-Za-z0-9._ -]", "_");
        fallback = fallback.replace("\"", "'");
        return fallback.isBlank() ? "download" : fallback;
    }
}
