package com.project.taskmanagement.service.impl;

import com.project.taskmanagement.dto.request.auth.ChangePasswordRequest;
import com.project.taskmanagement.dto.request.auth.LoginRequest;
import com.project.taskmanagement.dto.response.auth.AuthResponse;
import com.project.taskmanagement.entity.TokenSession;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.enums.AuditAction;
import com.project.taskmanagement.enums.SystemAuditAction;
import com.project.taskmanagement.enums.SystemAuditResourceType;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.TokenSessionRepository;
import com.project.taskmanagement.repository.UserRepository;
import com.project.taskmanagement.security.JwtTokenProvider;
import com.project.taskmanagement.security.LoginRateLimiter;
import com.project.taskmanagement.security.TokenHashHelper;
import com.project.taskmanagement.security.TokenBlacklistService;
import com.project.taskmanagement.service.AuditLogService;
import com.project.taskmanagement.service.AuthService;
import com.project.taskmanagement.service.SystemAuditService;
import com.project.taskmanagement.service.audit.AuditRequestHelper;
import com.project.taskmanagement.service.model.SystemAuditCommand;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {

    AuthenticationManager authenticationManager;
    JwtTokenProvider jwtTokenProvider;
    UserRepository userRepository;
    TokenSessionRepository tokenSessionRepository;
    TokenHashHelper tokenHashHelper;
    TokenBlacklistService tokenBlacklistService;
    LoginRateLimiter loginRateLimiter;
    AuditLogService auditLogService;
    PasswordEncoder passwordEncoder;
    SystemAuditService systemAuditService;
    AuditRequestHelper auditRequestHelper;
    HttpServletRequest httpServletRequest;

    @Override
    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(
                request.email()
        );

        String rateKey = "login:" + email;

        loginRateLimiter.check(rateKey);

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.password()
                    )
            );

        } catch (BadCredentialsException ex) {
            logAuthFailure(email, "Invalid credentials");
            throw new BusinessException(
                    ErrorCode.INVALID_CREDENTIALS
            );

        } catch (DisabledException ex) {
            logAuthFailure(email, "Account disabled");
            throw new BusinessException(
                    ErrorCode.ACCOUNT_DISABLED
            );
        }

        loginRateLimiter.reset(rateKey);

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.INVALID_CREDENTIALS
                        )
                );

        if (!user.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_DISABLED
            );
        }

        String accessTokenJti =
                UUID.randomUUID().toString();

        String refreshTokenJti =
                UUID.randomUUID().toString();

        TokenSession session = TokenSession.builder()
                .userId(user.getId())
                .accessTokenJti(accessTokenJti)
                .refreshTokenJti(refreshTokenJti)
                .revoked(false)
                .expiredAt(
                        jwtTokenProvider
                                .getRefreshTokenExpiry()
                )
                .build();

        String refreshToken =
                jwtTokenProvider.generateRefreshToken(
                        session.getId(),
                        refreshTokenJti
                );

        session.setRefreshTokenHash(
                tokenHashHelper.sha256(refreshToken)
        );

        tokenSessionRepository.save(session);

        String accessToken =
                jwtTokenProvider.generateAccessToken(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        accessTokenJti
                );

        auditLogService.log(
                user.getId(),
                AuditAction.LOGIN.name()
        );

        systemAuditService.log(
                new SystemAuditCommand(
                        user.getId(),
                        SystemAuditAction.LOGIN_SUCCESS,
                        SystemAuditResourceType.AUTH,
                        user.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        Map.of(
                                "username",
                                user.getUsername(),
                                "email",
                                user.getEmail()
                        ),
                        true,
                        null
                )
        );

        return new AuthResponse(
                accessToken,
                refreshToken
        );
    }

    @Override
    @Transactional
    public AuthResponse refresh(String refreshToken) {

        UUID sessionId;
        String refreshTokenJti;

        try {
            sessionId =
                    jwtTokenProvider
                            .getSessionIdFromRefreshToken(
                                    refreshToken
                            );

            refreshTokenJti =
                    jwtTokenProvider.getJti(refreshToken);

        } catch (ExpiredJwtException ex) {
            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_EXPIRED
            );

        } catch (JwtException |
                 IllegalArgumentException ex) {
            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_INVALID
            );
        }

        String refreshTokenHash =
                tokenHashHelper.sha256(refreshToken);

        TokenSession session =
                tokenSessionRepository
                        .findByIdAndRefreshTokenHashAndRevokedFalse(
                                sessionId,
                                refreshTokenHash
                        )
                        .orElseThrow(() ->
                                new BusinessException(
                                        ErrorCode.REFRESH_TOKEN_INVALID
                                )
                        );

        if (session.getRefreshTokenJti() == null
                || refreshTokenJti == null
                || !session.getRefreshTokenJti()
                .equals(refreshTokenJti)) {
            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_INVALID
            );
        }

        if (session.getExpiredAt()
                .isBefore(Instant.now())) {
            throw new BusinessException(
                    ErrorCode.REFRESH_TOKEN_EXPIRED
            );
        }

        User user = userRepository
                .findById(session.getUserId())
                .orElseThrow(() ->
                        new BusinessException(
                                ErrorCode.USER_NOT_FOUND
                        )
                );

        if (!user.isEnabled()) {
            throw new BusinessException(
                    ErrorCode.ACCOUNT_DISABLED
            );
        }

        session.revoke(Instant.now());
        tokenSessionRepository.save(session);

        String newAccessTokenJti =
                UUID.randomUUID().toString();

        String newRefreshTokenJti =
                UUID.randomUUID().toString();

        TokenSession newSession =
                TokenSession.builder()
                        .userId(user.getId())
                        .accessTokenJti(newAccessTokenJti)
                        .refreshTokenJti(newRefreshTokenJti)
                        .revoked(false)
                        .expiredAt(
                                jwtTokenProvider
                                        .getRefreshTokenExpiry()
                        )
                        .build();

        String newRefreshToken =
                jwtTokenProvider.generateRefreshToken(
                        newSession.getId(),
                        newRefreshTokenJti
                );

        newSession.setRefreshTokenHash(
                tokenHashHelper.sha256(newRefreshToken)
        );

        tokenSessionRepository.save(newSession);

        String newAccessToken =
                jwtTokenProvider.generateAccessToken(
                        user.getId(),
                        user.getUsername(),
                        user.getRole().name(),
                        newAccessTokenJti
                );

        systemAuditService.log(
                new SystemAuditCommand(
                        user.getId(),
                        SystemAuditAction.REFRESH_TOKEN_USED,
                        SystemAuditResourceType.AUTH,
                        session.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        Map.of(
                                "sessionId",
                                session.getId(),
                                "newSessionId",
                                newSession.getId(),
                                "userId",
                                user.getId()
                        ),
                        true,
                        null
                )
        );

        return new AuthResponse(
                newAccessToken,
                newRefreshToken
        );
    }

    @Override
    @Transactional
    public void logout(
            String accessToken,
            String refreshToken
    ) {
        UUID userId = null;

        try {
            userId = jwtTokenProvider
                    .getUserId(accessToken);

            Duration remainingDuration =
                    jwtTokenProvider
                            .getRemainingDuration(
                                    accessToken
                            );

            if (!remainingDuration.isZero() &&
                    !remainingDuration.isNegative()) {
                tokenBlacklistService.revoke(
                        accessToken,
                        remainingDuration
                );
            }

        } catch (ExpiredJwtException ex) {
            // Access token hết hạn thì không cần blacklist.

        } catch (JwtException |
                 IllegalArgumentException ex) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        String refreshTokenHash =
                tokenHashHelper.sha256(refreshToken);

        tokenSessionRepository
                .findByRefreshTokenHashAndRevokedFalse(
                        refreshTokenHash
                )
                .ifPresent(session -> {
                    session.revoke(Instant.now());
                    tokenSessionRepository.save(session);
                });

        if (userId != null) {
            auditLogService.log(
                    userId,
                    AuditAction.LOGOUT.name()
            );

            systemAuditService.log(
                    new SystemAuditCommand(
                            userId,
                            SystemAuditAction.LOGOUT,
                            SystemAuditResourceType.AUTH,
                            userId,
                            auditRequestHelper.getClientIp(httpServletRequest),
                            auditRequestHelper.getUserAgent(httpServletRequest),
                            null,
                            Map.of("userId", userId),
                            true,
                            null
                    )
            );
        }
    }

    @Override
    @Transactional
    public void changePassword(
            String username,
            ChangePasswordRequest request
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.CURRENT_PASSWORD_INVALID
            );
        }

        if (!request.newPassword()
                .equals(request.confirmPassword())) {
            throw new BusinessException(
                    ErrorCode.PASSWORD_CONFIRM_NOT_MATCH
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {
            throw new BusinessException(
                    ErrorCode.NEW_PASSWORD_SAME_AS_CURRENT
            );
        }

        Instant revokedAt =
                Instant.now();

        user.setPassword(
                passwordEncoder.encode(request.newPassword())
        );
        user.setLogoutAllAt(revokedAt);

        userRepository.save(user);

        // Thu hồi tất cả refresh token cũ.
        tokenSessionRepository.revokeAllByUserId(
                user.getId(),
                revokedAt
        );

        auditLogService.log(
                user.getId(),
                AuditAction.CHANGE_PASSWORD.name()
        );

        systemAuditService.log(
                new SystemAuditCommand(
                        user.getId(),
                        SystemAuditAction.PASSWORD_CHANGED,
                        SystemAuditResourceType.USER,
                        user.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        Map.of(
                                "userId",
                                user.getId(),
                                "logoutAllAt",
                                user.getLogoutAllAt()
                        ),
                        true,
                        null
                )
        );

        systemAuditService.log(
                new SystemAuditCommand(
                        user.getId(),
                        SystemAuditAction.REFRESH_TOKEN_REVOKED,
                        SystemAuditResourceType.AUTH,
                        user.getId(),
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        Map.of(
                                "reason",
                                "PASSWORD_CHANGED",
                                "userId",
                                user.getId()
                        ),
                        true,
                        null
                )
        );
    }

    @Override
    @Transactional
    public void logoutAll(String accessToken) {

        UUID userId;

        try {
            userId = jwtTokenProvider.getUserId(accessToken);

        } catch (ExpiredJwtException ex) {
            throw new BusinessException(
                    ErrorCode.TOKEN_EXPIRED
            );

        } catch (JwtException |
                 IllegalArgumentException ex) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        // Thu hồi toàn bộ refresh token của user.
        Instant revokedAt =
                Instant.now();

        tokenSessionRepository.revokeAllByUserId(
                userId,
                revokedAt
        );

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.USER_NOT_FOUND)
                );

        user.setLogoutAllAt(revokedAt);
        userRepository.save(user);

        // Đưa access token hiện tại vào blacklist
        // đúng bằng thời gian sống còn lại của token.
        Duration remainingDuration;

        try {
            remainingDuration =
                    jwtTokenProvider.getRemainingDuration(
                            accessToken
                    );

        } catch (ExpiredJwtException ex) {
            remainingDuration = Duration.ZERO;

        } catch (JwtException |
                 IllegalArgumentException ex) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        if (!remainingDuration.isZero()
                && !remainingDuration.isNegative()) {

            tokenBlacklistService.revoke(
                    accessToken,
                    remainingDuration
            );
        }

        auditLogService.log(
                userId,
                AuditAction.LOGOUT_ALL.name()
        );

        systemAuditService.log(
                new SystemAuditCommand(
                        userId,
                        SystemAuditAction.REFRESH_TOKEN_REVOKED,
                        SystemAuditResourceType.AUTH,
                        userId,
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        Map.of(
                                "reason",
                                "LOGOUT_ALL",
                                "userId",
                                userId
                        ),
                        true,
                        null
                )
        );

        systemAuditService.log(
                new SystemAuditCommand(
                        userId,
                        SystemAuditAction.LOGOUT,
                        SystemAuditResourceType.AUTH,
                        userId,
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        Map.of(
                                "scope",
                                "ALL_DEVICES",
                                "userId",
                                userId
                        ),
                        true,
                        null
                )
        );
    }
    //=======================HELPER=======================
    private void logAuthFailure(
            String email,
            String errorMessage
    ) {
        UUID userId =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .map(User::getId)
                        .orElse(null);

        Map<String, Object> value =
                new LinkedHashMap<>();
        value.put("email", email);

        systemAuditService.log(
                new SystemAuditCommand(
                        userId,
                        SystemAuditAction.LOGIN_FAILED,
                        SystemAuditResourceType.AUTH,
                        userId,
                        auditRequestHelper.getClientIp(httpServletRequest),
                        auditRequestHelper.getUserAgent(httpServletRequest),
                        null,
                        value,
                        false,
                        errorMessage
                )
        );
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }

        return email
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}


