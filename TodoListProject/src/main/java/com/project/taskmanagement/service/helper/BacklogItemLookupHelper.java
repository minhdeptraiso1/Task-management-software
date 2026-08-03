package com.project.taskmanagement.service.helper;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.repository.BacklogItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BacklogItemLookupHelper {

    private final BacklogItemRepository backlogItemRepository;

    public Map<UUID, BacklogItem> findBacklogItemMap(Collection<UUID> backlogItemIds) {
        if (backlogItemIds == null || backlogItemIds.isEmpty()) {
            return Map.of();
        }

        var distinctIds = backlogItemIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (distinctIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, BacklogItem> itemsById = new LinkedHashMap<>();
        backlogItemRepository.findAllById(distinctIds)
                .forEach(item -> itemsById.put(item.getId(), item));
        return itemsById;
    }

    public BacklogItem getOrNull(Map<UUID, BacklogItem> itemsById, UUID backlogItemId) {
        return backlogItemId == null || itemsById == null
                ? null
                : itemsById.get(backlogItemId);
    }
}
