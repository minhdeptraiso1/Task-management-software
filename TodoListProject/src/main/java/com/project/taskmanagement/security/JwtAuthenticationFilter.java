package com.project.taskmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.entity.User;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.repository.TokenSessionRepository;
import com.project.taskmanagement.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    static final String BEARER_PREFIX = "Bearer ";
    static final String TOKEN_TYPE_CLAIM = "tokenType";
    static final String ACCESS_TOKEN_TYPE = "ACCESS";

    JwtTokenProvider jwtTokenProvider;
    TokenBlacklistService tokenBlacklistService;
    UserRepository userRepository;
    TokenSessionRepository tokenSessionRepository;
    ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();

        return path.equals("/auth/login")
                || path.equals("/auth/refresh")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.equals("/actuator/health")
                || path.equals("/actuator/info");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

        if ((authorizationHeader == null || authorizationHeader.isBlank())
                && request.getParameter("access_token") != null
                && !request.getParameter("access_token").isBlank()) {
            authorizationHeader = BEARER_PREFIX + request.getParameter("access_token").trim();
        }

        /*
         * Không có Authorization header:
         * Không tự trả lỗi tại đây.
         *
         * Cho request đi tiếp để Spring Security xử lý.
         * Nếu endpoint cần đăng nhập thì AuthenticationEntryPoint
         * sẽ trả về lỗi 401 theo đúng JSON của hệ thống.
         */
        if (authorizationHeader == null
                || authorizationHeader.isBlank()) {

            filterChain.doFilter(request, response);
            return;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            writeErrorResponse(
                    request,
                    response,
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
            return;
        }

        String accessToken = authorizationHeader
                .substring(BEARER_PREFIX.length())
                .trim();

        if (accessToken.isBlank()) {
            writeErrorResponse(
                    request,
                    response,
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
            return;
        }

        try {
            // Access token đã logout thì không cho sử dụng lại.
            if (tokenBlacklistService.isRevoked(accessToken)) {
                writeErrorResponse(
                        request,
                        response,
                        ErrorCode.TOKEN_REVOKED
                );
                return;
            }

            Claims claims = jwtTokenProvider
                    .validateToken(accessToken)
                    .getBody();

            validateAccessTokenType(claims);

            UUID userId = UUID.fromString(
                    claims.getSubject()
            );

            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                writeErrorResponse(
                        request,
                        response,
                        ErrorCode.INVALID_ACCESS_TOKEN
                );
                return;
            }

            if (!user.isEnabled()) {
                writeErrorResponse(
                        request,
                        response,
                        ErrorCode.ACCOUNT_DISABLED
                );
                return;
            }

            if (isIssuedBeforeLogoutAll(claims, user)) {
                writeErrorResponse(
                        request,
                        response,
                        ErrorCode.TOKEN_REVOKED
                );
                return;
            }

            String accessTokenJti =
                    claims.getId();

            if (accessTokenJti == null
                    || accessTokenJti.isBlank()) {

                writeErrorResponse(
                        request,
                        response,
                        ErrorCode.INVALID_ACCESS_TOKEN
                );
                return;
            }

            if (!tokenSessionRepository
                    .existsByUserIdAndAccessTokenJtiAndRevokedFalse(
                            userId,
                            accessTokenJti
                    )) {
                writeErrorResponse(
                        request,
                        response,
                        ErrorCode.TOKEN_REVOKED
                );
                return;
            }

            String username = user.getUsername();
            String role = user.getRole().name();

            /*
             * Không ghi đè Authentication nếu SecurityContext
             * đã có thông tin đăng nhập.
             */
            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(
                                        new SimpleGrantedAuthority(
                                                "ROLE_" + role
                                        )
                                )
                        );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {

            SecurityContextHolder.clearContext();

            writeErrorResponse(
                    request,
                    response,
                    ErrorCode.TOKEN_EXPIRED
            );

        } catch (JwtException
                 | IllegalArgumentException ex) {

            SecurityContextHolder.clearContext();

            writeErrorResponse(
                    request,
                    response,
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }
    }

    private void validateAccessTokenType(
            Claims claims
    ) {
        String tokenType = claims.get(
                TOKEN_TYPE_CLAIM,
                String.class
        );

        if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
            throw new IllegalArgumentException(
                    "Token không phải access token"
            );
        }
    }

    private boolean isIssuedBeforeLogoutAll(
            Claims claims,
            User user
    ) {
        if (user.getLogoutAllAt() == null) {
            return false;
        }

        if (claims.getIssuedAt() == null) {
            return true;
        }

        Instant issuedAt = claims.getIssuedAt()
                .toInstant();

        return issuedAt.isBefore(user.getLogoutAllAt());
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            ErrorCode errorCode
    ) throws IOException {

        if (response.isCommitted()) {
            return;
        }

        response.setStatus(
                errorCode.status().value()
        );

        response.setCharacterEncoding(
                StandardCharsets.UTF_8.name()
        );

        response.setContentType(
                MediaType.APPLICATION_JSON_VALUE
        );

        ApiResponseSever<Void> responseBody =
                ApiResponseSever
                        .<Void>of(errorCode.code(), errorCode.message(), null)
                        .withPath(request.getRequestURI());

        objectMapper.writeValue(
                response.getWriter(),
                responseBody
        );
    }
}
