package com.project.taskmanagement.service.notification;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRecipientResolverTest {

    private final NotificationRecipientResolver resolver =
            new NotificationRecipientResolver();

    @Test
    void removesActorNullAndDuplicateRecipientsWhileKeepingOrder() {
        UUID actor = UUID.randomUUID();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        assertThat(resolver.normalizeRecipients(
                actor,
                Arrays.asList(first, null, actor, second, first)
        )).containsExactly(first, second);
    }

    @Test
    void handlesNullRecipientCollection() {
        assertThat(resolver.normalizeRecipients(UUID.randomUUID(), null))
                .isEmpty();
    }
}
