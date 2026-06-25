package com.project.taskmanagement.service.context;

import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.security.CurrentUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class CurrentUserService {

    UserRepository userRepository;

    /**
     * Lấy user hiện đang đăng nhập.
     */
    public User getCurrentUser() {
        String username =
                CurrentUser.username();

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );
    }

    /**
     * Lấy user hiện tại và bảo đảm tài khoản
     * vẫn đang hoạt động.
     */
    public User getActiveCurrentUser() {
        User user = getCurrentUser();

        if (!user.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_DISABLED
            );
        }

        return user;
    }
}