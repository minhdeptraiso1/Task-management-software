package com.project.taskmanagement.service.helper;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserLookupHelper {

    private final UserRepository userRepository;

    public Map<UUID, User> findUserMap(Collection<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        var distinctIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, User> usersById = new LinkedHashMap<>();
        userRepository.findAllById(distinctIds)
                .forEach(user -> usersById.put(user.getId(), user));
        return usersById;
    }

    public User getOrNull(Map<UUID, User> usersById, UUID userId) {
        return userId == null || usersById == null
                ? null
                : usersById.get(userId);
    }
}
