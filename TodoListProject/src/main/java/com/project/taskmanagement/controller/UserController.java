package com.project.taskmanagement.controller;

import com.project.taskmanagement.dto.request.user.CreateUserRequest;
import com.project.taskmanagement.dto.request.user.UpdateUserRequest;
import com.project.taskmanagement.dto.request.user.UserSearchRequest;
import com.project.taskmanagement.dto.response.core.ApiResponseSever;
import com.project.taskmanagement.dto.response.user.UserResponse;
import com.project.taskmanagement.dto.response.user.UserPageResponse;
import com.project.taskmanagement.enums.UserRole;
import com.project.taskmanagement.security.CurrentUser;
import com.project.taskmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "User",
        description = "API quản lý tài khoản người dùng"
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(
        level = AccessLevel.PRIVATE,
        makeFinal = true
)
public class UserController {

    UserService userService;

    // ===================== CURRENT USER =====================

    @Operation(
            summary = "Lấy thông tin người dùng hiện tại",
            description = """
                    Trả về thông tin của người dùng đang đăng nhập
                    dựa trên JWT access token.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy thông tin người dùng thành công"
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
    @GetMapping("/me")
    public ApiResponseSever<UserResponse> getCurrentUser() {
        return ApiResponseSever.ok(
                userService.getCurrentUser(
                        CurrentUser.username()
                )
        );
    }

    // ===================== SEARCH USERS =====================

    @Operation(
            summary = "Tìm kiếm người dùng",
            description = """
                    Tìm kiếm người dùng theo từ khóa, vai trò
                    và trạng thái hoạt động.

                    Hỗ trợ phân trang và sắp xếp.
                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tìm kiếm người dùng thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Tham số tìm kiếm không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc access token không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ApiResponseSever<UserPageResponse> searchUsers(
            @ParameterObject
            UserSearchRequest request,

            @ParameterObject
            Pageable pageable
    ) {
        return ApiResponseSever.ok(
                userService.searchUsers(
                        request,
                        pageable
                )
        );
    }

    // ===================== CREATE USER =====================

    @Operation(
            summary = "Tạo người dùng mới",
            description = """
                    Tạo một tài khoản người dùng mới.
                    Username và email không được trùng.
                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tạo người dùng thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu gửi lên không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc access token không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username hoặc email đã tồn tại"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponseSever<UserResponse> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin người dùng cần tạo",
                    required = true
            )
            @RequestBody
            @Valid
            CreateUserRequest request
    ) {
        return ApiResponseSever.ok(
                userService.createUser(request)
        );
    }

    // ===================== UPDATE USER =====================

    @Operation(
            summary = "Cập nhật người dùng",
            description = """
                    Cập nhật mật khẩu, vai trò hoặc trạng thái
                    của người dùng theo ID.

                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật người dùng thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dữ liệu gửi lên không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc access token không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập"
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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponseSever<UserResponse> updateUser(
            @Parameter(
                    description = "ID của người dùng cần cập nhật",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable
            UUID id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin cần cập nhật",
                    required = true
            )
            @RequestBody
            @Valid
            UpdateUserRequest request
    ) {
        return ApiResponseSever.ok(
                userService.updateUser(
                        id,
                        request
                )
        );
    }

    // ===================== DELETE USER =====================

    @Operation(
            summary = "Xóa mềm người dùng",
            description = """
                    Xóa mềm tài khoản người dùng theo ID.
                    Người dùng bị xóa sẽ bị vô hiệu hóa và không
                    xuất hiện trong các truy vấn thông thường.

                    Chỉ ADMIN được phép sử dụng API này.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Xóa người dùng thành công"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "ID người dùng không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc access token không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập"
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
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponseSever<Void> deleteUser(
            @Parameter(
                    description = "ID của người dùng cần xóa",
                    required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable
            UUID id
    ) {
        userService.deleteUserById(id);

        return ApiResponseSever.ok(null);
    }
    // ===================== USER ROLES =====================
    @Operation(
            summary = "Lấy danh sách vai trò người dùng",
            description = """
                Trả về toàn bộ vai trò có thể gán cho người dùng.
                API chỉ dành cho ADMIN.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Lấy danh sách vai trò thành công"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Chưa đăng nhập hoặc access token không hợp lệ"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Không có quyền truy cập"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Lỗi hệ thống"
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public ApiResponseSever<List<UserRole>> getUserRoles() {
        return ApiResponseSever.ok(
                List.of(UserRole.values())
        );
    }
}
