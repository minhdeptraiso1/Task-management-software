package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.config.CacheNames;
import com.project.taskmanagement.dto.request.user.CreateUserRequest;
import com.project.taskmanagement.dto.request.user.UpdateUserRequest;
import com.project.taskmanagement.dto.request.user.UserSearchRequest;
import com.project.taskmanagement.dto.response.user.UserResponse;
import com.project.taskmanagement.dto.response.user.UserPageResponse;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.mapper.UserMapper;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.repository.spec.UserSpecification;
import com.project.taskmanagement.security.CurrentUser;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import jakarta.servlet.http.HttpServletRequest;
import com.project.taskmanagement.service.UserService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    SystemAuditService systemAuditService;
    AuditRequestHelper auditRequestHelper;
    HttpServletRequest httpServletRequest;

    @Override
    @Cacheable(
            value = CacheNames.USER_CURRENT,
            key = "#username"
    )
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    @Cacheable(
            value = CacheNames.USER_SEARCH,
            key = "'keyword=' + (#request == null || #request.keyword() == null ? '' : #request.keyword()) " +
                    "+ '|role=' + (#request == null || #request.role() == null ? '' : #request.role()) " +
                    "+ '|enabled=' + (#request == null || #request.enabled() == null ? '' : #request.enabled()) " +
                    "+ '|page=' + #pageable.pageNumber " +
                    "+ '|size=' + #pageable.pageSize " +
                    "+ '|sort=' + #pageable.sort.toString()"
    )
    public UserPageResponse searchUsers(
            UserSearchRequest request,
            Pageable pageable
    ) {
        String keyword =
                request != null
                        ? request.keyword()
                        : null;

        UserRole role =
                request != null
                        ? request.role()
                        : null;

        Boolean enabled =
                request != null
                        ? request.enabled()
                        : null;

        Specification<User> specification =
                Specification.allOf(
                        UserSpecification.search(keyword),
                        UserSpecification.hasRole(role),
                        UserSpecification.isEnabled(enabled)
                );

        Page<UserResponse> userPage =
                userRepository
                        .findAll(specification, pageable)
                        .map(userMapper::toResponse);

        return UserPageResponse.from(userPage);
    }

    @Override
    @Transactional
    @Cacheable(
            value = CacheNames.USER_PROJECT_CANDIDATE_SEARCH,
            key = "T(com.project.taskmanagement.security.CurrentUser).username() " +
                    "+ '|keyword=' + (#request == null || #request.keyword() == null ? '' : #request.keyword()) " +
                    "+ '|role=' + (#request == null || #request.role() == null ? '' : #request.role()) " +
                    "+ '|page=' + #pageable.pageNumber " +
                    "+ '|size=' + #pageable.pageSize " +
                    "+ '|sort=' + #pageable.sort.toString()"
    )
    public UserPageResponse searchProjectCandidateUsers(
            UserSearchRequest request,
            Pageable pageable
    ) {
        String keyword =
                request != null
                        ? request.keyword()
                        : null;

        UserRole role =
                request != null
                        ? request.role()
                        : null;

        Specification<User> specification =
                Specification.allOf(
                        UserSpecification.search(keyword),
                        UserSpecification.hasRole(role),
                        UserSpecification.isEnabled(true),
                        UserSpecification.roleIsNot(UserRole.ADMIN)
                );

        Page<UserResponse> userPage =
                userRepository
                        .findAll(specification, pageable)
                        .map(userMapper::toResponse);

        return UserPageResponse.from(userPage);
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_DETAIL, key = "#userId"),
            @CacheEvict(value = CacheNames.USER_CURRENT, allEntries = true),
            @CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.USER_PROJECT_CANDIDATE_SEARCH, allEntries = true),
            @CacheEvict(
                    value = CacheNames.MY_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.MY_TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.MY_TIME_SUMMARY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_MEMBER_LIST,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_ACTIVITY_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_ACTIVITY_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public void deleteUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setDeletedAt(Instant.now());
        user.setDeletedBy(CurrentUser.username());

        userRepository.save(user);
    }


    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true),
            @CacheEvict(value = CacheNames.USER_PROJECT_CANDIDATE_SEARCH, allEntries = true),
            @CacheEvict(
                    value = CacheNames.MY_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.MY_TASK_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.MY_TIME_SUMMARY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_MEMBER_LIST,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_WORKLOAD,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_DASHBOARD_RECENT_ACTIVITY,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_ACTIVITY_SEARCH,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_ACTIVITY_DETAIL,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_SPRINT,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_MEMBER,
                    allEntries = true
            ),
            @CacheEvict(
                    value = CacheNames.PROJECT_REPORT_TIME,
                    allEntries = true
            )
    })
    public UserResponse createUser(
            CreateUserRequest request
    ) {
        String username = normalizeUsername(
                request.username()
        );

        String email = normalizeEmail(
                request.email()
        );

        if (userRepository
                .existsByUsernameIgnoreCase(username)) {
            throw new BusinessException(
                    ErrorCode.USERNAME_ALREADY_EXISTS
            );
        }

        if (userRepository
                .existsByEmailIgnoreCase(email)) {
            throw new BusinessException(
                    ErrorCode.EMAIL_ALREADY_EXISTS
            );
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .password(
                        passwordEncoder.encode(
                                request.password()
                        )
                )
                .role(request.role())
                .enabled(true)
                .build();

        User savedUser =
                userRepository.save(user);

        systemAuditService.log(
                new SystemAuditCommand(
                        currentActorIdOrNull(),
                        SystemAuditAction.USER_CREATED,
                        SystemAuditResourceType.USER,
                        savedUser.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        snapshot(savedUser),
                        true,
                        null
                )
        );

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    @Caching(
            put = {
                    @CachePut(value = CacheNames.USER_DETAIL, key = "#id")
            },
            evict = {
                    @CacheEvict(value = CacheNames.USER_CURRENT, allEntries = true),
                    @CacheEvict(value = CacheNames.USER_SEARCH, allEntries = true),
                    @CacheEvict(value = CacheNames.USER_PROJECT_CANDIDATE_SEARCH, allEntries = true)
            }
    )
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.role() != null) {
            user.setRole(request.role());
        }

        if (request.enabled() != null) {
            user.setEnabled(request.enabled());
        }

        userRepository.save(user);

        return userMapper.toResponse(user);
    }
    //=================HELPER================
    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return "";
        }

        return username.trim();
    }

    private UUID currentActorIdOrNull() {
        try {
            return userRepository
                    .findByUsername(CurrentUser.username())
                    .map(User::getId)
                    .orElse(null);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private Map<String, Object> snapshot(User user) {
        Map<String, Object> value =
                new LinkedHashMap<>();

        value.put("id", user.getId());
        value.put("username", user.getUsername());
        value.put("email", user.getEmail());
        value.put("role", user.getRole());
        value.put("enabled", user.isEnabled());
        value.put("createdAt", user.getCreatedAt());
        value.put("updatedAt", user.getUpdatedAt());

        return value;
    }
}
