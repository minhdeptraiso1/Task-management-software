package com.project.taskmanagement.service.mention;

import com.project.taskmanagement.entity.ProjectMember;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.ProjectMemberRepository;
import com.project.taskmanagement.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class CommentMentionResolverImpl
        implements CommentMentionResolver {

    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;

    @Override
    public List<MentionedUser> resolveProjectMentions(
            UUID projectId,
            String content
    ) {
        Set<String> requestedUsernames =
                CommentMentionParser
                        .parseUsernames(content)
                        .stream()
                        .map(username ->
                                username.toLowerCase(Locale.ROOT)
                        )
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        if (requestedUsernames.isEmpty()) {
            return List.of();
        }

        List<ProjectMember> members =
                projectMemberRepository
                        .findAllByProjectIdOrderByJoinedAtAsc(projectId);

        if (members.isEmpty()) {
            return List.of();
        }

        Map<UUID, User> usersById =
                userRepository
                        .findAllById(
                                members.stream()
                                        .map(ProjectMember::getUserId)
                                        .toList()
                        )
                        .stream()
                        .filter(User::isEnabled)
                        .collect(Collectors.toMap(
                                User::getId,
                                Function.identity(),
                                (first, second) -> first
                        ));

        List<MentionedUser> resolvedUsers =
                new ArrayList<>();

        Set<UUID> addedUserIds =
                new LinkedHashSet<>();

        for (ProjectMember member : members) {
            User user =
                    usersById.get(member.getUserId());

            if (user == null
                    || user.getUsername() == null) {
                continue;
            }

            String normalizedUsername =
                    user.getUsername()
                            .toLowerCase(Locale.ROOT);

            if (requestedUsernames.contains(normalizedUsername)
                    && addedUserIds.add(user.getId())) {
                resolvedUsers.add(
                        new MentionedUser(
                                user.getId(),
                                user.getUsername()
                        )
                );
            }
        }

        return resolvedUsers;
    }
}
