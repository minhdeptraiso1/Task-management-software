package com.project.taskmanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.core.ErrorResponseSever;
import com.project.taskmanagement.exception.ErrorCode;
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
import java.util.List;

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
                || path.equals("/actuator/health");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader("Authorization");

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
                    response,
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
            return;
        }

        try {
            // Access token đã logout thì không cho sử dụng lại.
            if (tokenBlacklistService.isRevoked(accessToken)) {
                writeErrorResponse(
                        response,
                        ErrorCode.TOKEN_REVOKED
                );
                return;
            }

            Claims claims = jwtTokenProvider
                    .validateToken(accessToken)
                    .getBody();

            validateAccessTokenType(claims);

            String username = claims.get(
                    "username",
                    String.class
            );

            String role = claims.get(
                    "role",
                    String.class
            );

            if (username == null
                    || username.isBlank()
                    || role == null
                    || role.isBlank()) {

                writeErrorResponse(
                        response,
                        ErrorCode.INVALID_ACCESS_TOKEN
                );
                return;
            }

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
                    response,
                    ErrorCode.TOKEN_EXPIRED
            );

        } catch (JwtException
                 | IllegalArgumentException ex) {

            SecurityContextHolder.clearContext();

            writeErrorResponse(
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

    private void writeErrorResponse(
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
                ApiResponseSever.error(
                        new ErrorResponseSever(
                                errorCode.code(),
                                errorCode.message()
                        )
                );

        objectMapper.writeValue(
                response.getWriter(),
                responseBody
        );
    }
}