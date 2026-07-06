package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.entity.ProjectActivityLog;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.service.ProjectActivityDisplayService;
import com.project.taskmanagement.service.activity.ProjectActivityMessageResolver;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class ProjectActivityDisplayServiceImpl
        implements ProjectActivityDisplayService {

    UserRepository userRepository;

    ProjectActivityMessageResolver
            projectActivityMessageResolver;

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, String> resolveUsernames(
            Collection<ProjectActivityLog> activities
    ) {
        if (activities == null
                || activities.isEmpty()) {

            return Collections.emptyMap();
        }

        List<UUID> userIds =
                activities.stream()
                        .map(
                                ProjectActivityLog
                                        ::getPerformedByUserId
                        )
                        .filter(userId ->
                                userId != null
                        )
                        .distinct()
                        .toList();

        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<UUID, User> usersById =
                userRepository
                        .findAllById(userIds)
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        User::getId,
                                        Function.identity(),
                                        (left, right) -> left,
                                        LinkedHashMap::new
                                )
                        );

        Map<UUID, String> usernames =
                new LinkedHashMap<>();

        for (UUID userId : userIds) {
            User user =
                    usersById.get(userId);

            if (user == null) {
                usernames.put(
                        userId,
                        "Người dùng không tồn tại"
                );

                continue;
            }

            usernames.put(
                    userId,
                    user.getUsername()
            );
        }

        return usernames;
    }

    @Override
    public String resolveDisplayMessage(
            ProjectActivityLog activity,
            String performerName
    ) {
        return projectActivityMessageResolver
                .resolve(
                        activity,
                        performerName
                );
    }
}