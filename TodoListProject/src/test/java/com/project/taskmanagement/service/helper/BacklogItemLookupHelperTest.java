package com.project.taskmanagement.service.helper;

import com.project.taskmanagement.entity.BacklogItem;
import com.project.taskmanagement.repository.BacklogItemRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class BacklogItemLookupHelperTest {

    private final BacklogItemRepository repository = mock(BacklogItemRepository.class);
    private final BacklogItemLookupHelper helper = new BacklogItemLookupHelper(repository);

    @Test
    void findBacklogItemMapLoadsDistinctIdsInOneQuery() {
        UUID itemId = UUID.randomUUID();
        BacklogItem item = mock(BacklogItem.class);
        when(item.getId()).thenReturn(itemId);
        when(repository.findAllById(List.of(itemId))).thenReturn(List.of(item));

        Map<UUID, BacklogItem> result = helper.findBacklogItemMap(List.of(itemId, itemId));

        assertThat(result).containsEntry(itemId, item);
        verify(repository, times(1)).findAllById(List.of(itemId));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void lookupIsNullSafe() {
        assertThat(helper.findBacklogItemMap(null)).isEmpty();
        assertThat(helper.getOrNull(Map.of(), null)).isNull();
        verifyNoInteractions(repository);
    }
}
