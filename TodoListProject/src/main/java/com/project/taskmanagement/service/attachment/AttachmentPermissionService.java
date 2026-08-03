package com.project.taskmanagement.service.attachment;

import com.project.taskmanagement.entity.Attachment;
import com.project.taskmanagement.entity.Project;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.service.access.ProjectAccessService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AttachmentPermissionService {

    ProjectAccessService projectAccessService;

    public void requireView(
            Project project,
            User currentUser
    ) {
        projectAccessService.requireViewAccess(
                project,
                currentUser
        );
    }

    public void requireUpload(
            Project project,
            User currentUser
    ) {
        projectAccessService.requireViewAccess(
                project,
                currentUser
        );

        if (currentUser.getRole() == UserRole.ADMIN) {
            throw new BusinessException(
                    ErrorCode.ATTACHMENT_ACCESS_DENIED
            );
        }

        projectAccessService.getMembershipOrThrow(
                project.getId(),
                currentUser.getId()
        );
    }

    public void requireDelete(
            Attachment attachment,
            User currentUser
    ) {
        if (attachment.getUploadedByUserId()
                .equals(currentUser.getId())) {
            return;
        }

        throw new BusinessException(
                ErrorCode.ATTACHMENT_ACCESS_DENIED
        );
    }

    public boolean canDelete(
            Attachment attachment,
            User currentUser
    ) {
        return attachment.getUploadedByUserId()
                .equals(currentUser.getId());
    }
}
