package com.project.taskmanagement.service.helper;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class UserLookupHelperTest {

    private final UserRepository repository = mock(UserRepository.class);
    private final UserLookupHelper helper = new UserLookupHelper(repository);

    @Test
    void findUserMapLoadsDistinctNonNullIdsInOneQuery() {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        User first = mock(User.class);
        User second = mock(User.class);
        when(first.getId()).thenReturn(firstId);
        when(second.getId()).thenReturn(secondId);
        when(repository.findAllById(List.of(firstId, secondId)))
                .thenReturn(List.of(first, second));

        Map<UUID, User> result = helper.findUserMap(
                java.util.Arrays.asList(firstId, null, firstId, secondId)
        );

        assertThat(result).containsEntry(firstId, first).containsEntry(secondId, second);
        verify(repository, times(1)).findAllById(List.of(firstId, secondId));
        verifyNoMoreInteractions(repository);
    }

    @Test
    void findUserMapSkipsRepositoryForEmptyInput() {
        assertThat(helper.findUserMap(List.of())).isEmpty();
        assertThat(helper.getOrNull(Map.of(), null)).isNull();
        verifyNoInteractions(repository);
    }
}
