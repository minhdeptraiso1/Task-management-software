package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.auth.ChangePasswordRequest;
import com.project.taskmanagement.dto.request.auth.LoginRequest;
import com.project.taskmanagement.dto.request.auth.LogoutRequest;
import com.project.taskmanagement.dto.request.auth.RefreshTokenRequest;
import com.project.taskmanagement.dto.response.auth.AuthResponse;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.enums.RateLimitAction;
import com.project.taskmanagement.exception.BusinessException;
import com.project.taskmanagement.exception.ErrorCode;
import com.project.taskmanagement.security.CurrentUser;
import com.project.taskmanagement.service.AuthService;
import com.project.taskmanagement.service.RateLimitService;
import com.project.taskmanagement.util.ClientIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "Auth",
        description = "API xác thực, quản lý mật khẩu và phiên đăng nhập"
)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class AuthController {

    AuthService authService;
    RateLimitService rateLimitService;

    // ===================== LOGIN =====================

    @Operation(
            summary = "Đăng nhập",
            description = """
                    Đăng nhập bằng email và mật khẩu.
                    Nếu thông tin hợp lệ, hệ thống trả về access token
                    và refresh token.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Đăng nhập thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu gửi lên không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Email hoặc mật khẩu không đúng"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Tài khoản đã bị khóa"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Đăng nhập sai quá nhiều lần"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PostMapping("/login")
    public ApiResponseSever<AuthResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin đăng nhập",
                    required = true
            )
            @RequestBody
            @Valid
            LoginRequest request,

            HttpServletRequest httpRequest
    ) {
        String ip =
                ClientIpResolver.resolve(httpRequest);

        rateLimitService.check(
                RateLimitAction.LOGIN,
                ip + ":" + request.email()
        );

        return ApiResponseSever.ok(
                authService.login(request)
        );
    }

    // ===================== REFRESH TOKEN =====================

    @Operation(
            summary = "Làm mới access token",
            description = """
                    Cấp access token mới dựa trên refresh token.
                    Refresh token phải còn hiệu lực và chưa bị thu hồi.
                    """,
            security = {}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Làm mới access token thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token không được để trống"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token không hợp lệ, hết hạn hoặc đã bị thu hồi"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PostMapping("/refresh")
    public ApiResponseSever<AuthResponse> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token cần sử dụng",
                    required = true
            )
            @RequestBody
            @Valid
            RefreshTokenRequest request,

            HttpServletRequest httpRequest
    ) {
        String ip =
                ClientIpResolver.resolve(httpRequest);

        rateLimitService.check(
                RateLimitAction.REFRESH_TOKEN,
                ip
        );

        return ApiResponseSever.ok(
                authService.refresh(request.refreshToken())
        );
    }

    // ===================== LOGOUT =====================

    @Operation(
            summary = "Đăng xuất",
            description = """
                    Đăng xuất khỏi phiên hiện tại.
                    Access token được đưa vào blacklist và refresh token
                    tương ứng được đánh dấu đã thu hồi.
                    """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Đăng xuất thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Refresh token không được để trống"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Access token không hợp lệ hoặc chưa đăng nhập"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PostMapping("/logout")
    public ApiResponseSever<Void> logout(
            @Parameter(
                    description = "Access token theo định dạng Bearer &lt;access_token&gt;",
                    required = true
            )
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token cần thu hồi",
                    required = true
            )
            @RequestBody
            @Valid
            LogoutRequest request
    ) {
        String accessToken = extractBearerToken(authHeader);

        authService.logout(
                accessToken,
                request.refreshToken()
        );

        return ApiResponseSever.ok(null);
    }

    // ===================== CHANGE PASSWORD =====================

    @Operation(
            summary = "Đổi mật khẩu",
            description = """
                    Người dùng đang đăng nhập thực hiện đổi mật khẩu.
                    Mật khẩu hiện tại phải chính xác.
                    Mật khẩu mới phải khớp với mật khẩu xác nhận
                    và không được trùng với mật khẩu hiện tại.
                    """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Đổi mật khẩu thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Mật khẩu hiện tại không chính xác,
                            mật khẩu xác nhận không khớp hoặc
                            mật khẩu mới trùng với mật khẩu hiện tại
                            """
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc access token không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy người dùng"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PostMapping("/change-password")
    public ApiResponseSever<Void> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin đổi mật khẩu",
                    required = true
            )
            @RequestBody
            @Valid
            ChangePasswordRequest request
    ) {
        authService.changePassword(
                CurrentUser.username(),
                request
        );

        return ApiResponseSever.ok(null);
    }

    // ===================== LOGOUT ALL =====================

    @Operation(
            summary = "Đăng xuất khỏi tất cả thiết bị",
            description = """
                Thu hồi toàn bộ refresh token của người dùng.
                Access token của phiên hiện tại cũng được đưa
                vào blacklist và không thể tiếp tục sử dụng.
                """,
            security = {
                    @SecurityRequirement(name = "bearerAuth")
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Đăng xuất khỏi tất cả thiết bị thành công"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập, token hết hạn hoặc access token không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PostMapping("/logout-all")
    public ApiResponseSever<Void> logoutAll(
            @Parameter(
                    description = "Access token theo định dạng Bearer &lt;access_token&gt;",
                    required = true
            )
            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authHeader
    ) {
        String accessToken =
                extractBearerToken(authHeader);

        authService.logoutAll(accessToken);

        return ApiResponseSever.ok(null);
    }

    // ===================== PRIVATE HELPERS =====================

    private String extractBearerToken(String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ") ||
                authHeader.length() <= 7) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        String accessToken = authHeader
                .substring(7)
                .trim();

        if (accessToken.isBlank()) {
            throw new BusinessException(
                    ErrorCode.INVALID_ACCESS_TOKEN
            );
        }

        return accessToken;
    }
}

