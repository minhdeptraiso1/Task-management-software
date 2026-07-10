package com.project.taskmanagement.service.mention;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommentMentionParser {

    private static final Pattern MENTION_PATTERN =
            Pattern.compile("(?<![\\p{Alnum}_])@([\\p{L}\\p{N}._-]{2,100})");

    private CommentMentionParser() {
    }

    public static Set<String> parseUsernames(
            String content
    ) {
        Set<String> usernames =
                new LinkedHashSet<>();

        if (content == null
                || content.isBlank()) {
            return usernames;
        }

        Matcher matcher =
                MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            usernames.add(
                    matcher.group(1)
                            .trim()
            );
        }

        return usernames;
    }
}
