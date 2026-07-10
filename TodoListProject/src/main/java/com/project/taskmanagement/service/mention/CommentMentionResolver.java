package com.project.taskmanagement.service.mention;

import java.util.List;
import java.util.UUID;

public interface CommentMentionResolver {

    List<MentionedUser> resolveProjectMentions(
            UUID projectId,
            String content
    );
}
