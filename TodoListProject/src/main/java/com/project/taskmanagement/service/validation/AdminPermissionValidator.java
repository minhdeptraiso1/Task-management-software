package com.project.taskmanagement.service.validation;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.service.context.CurrentUserService;
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
public class AdminPermissionValidator {

    CurrentUserService currentUserService;

    public User requireAdmin() {
        User currentUser =
                currentUserService.getActiveCurrentUser();

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new BusinessException(
                    ErrorCode.ADMIN_ONLY
            );
        }

        return currentUser;
    }
}
