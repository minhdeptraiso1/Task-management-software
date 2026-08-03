package com.project.taskmanagement.repository;

import com.project.taskmanagement.entity.Notification;
import com.project.taskmanagement.entity.NotificationRecipient;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.repository.projection.NotificationView;
import com.project.taskmanagement.repository.support.RepositoryTestBase;
import com.project.taskmanagement.repository.support.TestEntityFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationRecipientRepositoryTest extends RepositoryTestBase {

    @Autowired UserRepository userRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired NotificationRecipientRepository notificationRecipientRepository;

    @Test
    void countsUnreadAndReturnsNotificationProjection() {
        User actor = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-notification-actor",
                "repo-notification-actor@test.local",
                UserRole.MANAGER
        ));
        User recipient = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-notification-recipient",
                "repo-notification-recipient@test.local",
                UserRole.EMPLOYEE
        ));
        Project project = projectRepository.saveAndFlush(TestEntityFactory.project(
                "REPO-NOTIFICATION",
                "Notification project",
                actor.getId()
        ));

        Notification unreadNotification = notificationRepository.saveAndFlush(
                TestEntityFactory.notification(actor.getId(), project.getId(), project.getId())
        );
        Notification readNotification = notificationRepository.saveAndFlush(
                TestEntityFactory.notification(actor.getId(), project.getId(), project.getId())
        );
        NotificationRecipient unread = TestEntityFactory.recipient(
                unreadNotification.getId(), recipient.getId(), null
        );
        NotificationRecipient read = TestEntityFactory.recipient(
                readNotification.getId(), recipient.getId(), Instant.now()
        );
        notificationRecipientRepository.saveAllAndFlush(java.util.List.of(unread, read));

        assertThat(notificationRecipientRepository.countUnreadByUserId(recipient.getId()))
                .isEqualTo(1L);

        Page<NotificationView> unreadPage =
                notificationRecipientRepository.searchNotificationViews(
                        recipient.getId(),
                        null,
                        true,
                        PageRequest.of(0, 10)
                );

        assertThat(unreadPage.getTotalElements()).isEqualTo(1L);
        assertThat(unreadPage.getContent().getFirst().getId())
                .isEqualTo(unreadNotification.getId());
        assertThat(unreadPage.getContent().getFirst().getReadAt()).isNull();
    }

    @Test
    void softDeletedRecipientIsExcludedFromUnreadCount() {
        User actor = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-notification-delete-actor",
                "repo-notification-delete-actor@test.local",
                UserRole.MANAGER
        ));
        User recipient = userRepository.saveAndFlush(TestEntityFactory.user(
                "repo-notification-delete-recipient",
                "repo-notification-delete-recipient@test.local",
                UserRole.EMPLOYEE
        ));
        Notification notification = notificationRepository.saveAndFlush(
                TestEntityFactory.notification(actor.getId(), null, null)
        );
        NotificationRecipient deleted = TestEntityFactory.recipient(
                notification.getId(), recipient.getId(), null
        );
        deleted.markDeleted(actor.getUsername());
        notificationRecipientRepository.saveAndFlush(deleted);

        assertThat(notificationRecipientRepository.countUnreadByUserId(recipient.getId()))
                .isZero();
    }
}
