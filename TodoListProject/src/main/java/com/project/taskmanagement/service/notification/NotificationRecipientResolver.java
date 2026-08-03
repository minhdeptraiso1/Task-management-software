package com.project.taskmanagement.service.notification;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

@Component
public class NotificationRecipientResolver {

    public List<UUID> normalizeRecipients(
            UUID actorUserId,
            Collection<UUID> recipientUserIds
    ) {
        if (recipientUserIds == null
                || recipientUserIds.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<UUID> recipients =
                new LinkedHashSet<>(recipientUserIds);

        recipients.remove(null);
        recipients.remove(actorUserId);

        return List.copyOf(recipients);
    }
}
