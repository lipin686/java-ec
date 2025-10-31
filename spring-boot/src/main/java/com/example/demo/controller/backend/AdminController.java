package com.example.demo.controller.backend;

import com.example.demo.dto.request.backend.CreateUserRequest;
import com.example.demo.dto.request.backend.UpdateUserStatusRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.AccountStatusException;
import com.example.demo.service.backend.AdminService;
import com.example.demo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 後台管理 Controller
 * 只有管理員可以訪問
 */
@RestController
@RequestMapping("/admin/v1")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    /**
     * 後台管理員登入
     * 不需要權限檢查，因為這是登入端點
     */
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(@Valid @RequestBody LoginRequest request) {
        // 先進行一般登入驗證
        LoginResponse response = authService.login(request);

        // 檢查是否具有管理員角色
        UserResponse user = response.getUser();
        // 修改：檢查是否包含 ADMIN 角色，而不是只能是 ADMIN 角色
        if (!user.getRoles().contains(UserRole.ADMIN)) {
            throw new AccountStatusException("此帳號沒有管理員權限");
        }

        return ResponseEntity.ok(ApiResponse.success("後台登入成功", response));
    }

    /**
     * 創建管理員帳號
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse<UserResponse>> createAdmin(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        String createdBy = authentication.getName();
        // 強制設置為管理員角色
        request.setRole(UserRole.ADMIN);
        UserResponse user = adminService.createAdmin(request, createdBy);
        return ResponseEntity.ok(ApiResponse.success("管理員創建成功", user));
    }

    /**
     * 創建前台用戶帳號
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            Authentication authentication) {
        String createdBy = authentication.getName();
        // 強制設置為前台用戶角色
        request.setRole(UserRole.USER);
        UserResponse user = adminService.createUser(request, createdBy);
        return ResponseEntity.ok(ApiResponse.success("用戶創建成功", user));
    }

    /**
     * 獲取所有用戶列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("獲取用戶列表成功", users));
    }

    /**
     * 根據角色獲取用戶列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable UserRole role) {
        List<UserResponse> users = adminService.getUsersByRole(role);
        return ResponseEntity.ok(ApiResponse.success("獲取用戶列表成功", users));
    }

    /**
     * 獲取所有管理員
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admins")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllAdmins() {
        List<UserResponse> admins = adminService.getUsersByRole(UserRole.ADMIN);
        return ResponseEntity.ok(ApiResponse.success("獲取管理員列表成功", admins));
    }

    /**
     * 獲取所有前台用戶
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/frontend-users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllFrontendUsers() {
        List<UserResponse> users = adminService.getUsersByRole(UserRole.USER);
        return ResponseEntity.ok(ApiResponse.success("獲取前台用戶列表成功", users));
    }

    /**
     * 停用/啟用用戶
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(
            @PathVariable Long userId,
            Authentication authentication) {
        String operatorEmail = authentication.getName();
        UserResponse user = adminService.toggleUserStatus(userId, operatorEmail);
        return ResponseEntity.ok(ApiResponse.success("用戶狀態更新成功", user));
    }

    /**
     * 軟刪除用戶
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            Authentication authentication) {
        String deletedBy = authentication.getName();
        adminService.deleteUser(userId, deletedBy);
        return ResponseEntity.ok(ApiResponse.success("用戶刪除成功", null));
    }

    /**
     * 恢復已刪除的用戶
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/restore")
    public ResponseEntity<ApiResponse<UserResponse>> restoreUser(
            @PathVariable Long userId,
            Authentication authentication) {
        String restoredBy = authentication.getName();
        UserResponse user = adminService.restoreUser(userId, restoredBy);
        return ResponseEntity.ok(ApiResponse.success("用戶恢復成功", user));
    }

    /**
     * 為用戶添加角色
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/add-role/{role}")
    public ResponseEntity<ApiResponse<UserResponse>> addUserRole(
            @PathVariable Long userId,
            @PathVariable UserRole role,
            Authentication authentication) {
        String operatorEmail = authentication.getName();
        UserResponse user = adminService.addUserRole(userId, role, operatorEmail);
        return ResponseEntity.ok(ApiResponse.success("角色添加成功", user));
    }

    /**
     * 移除用戶角色
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/{userId}/remove-role/{role}")
    public ResponseEntity<ApiResponse<UserResponse>> removeUserRole(
            @PathVariable Long userId,
            @PathVariable UserRole role,
            Authentication authentication) {
        String operatorEmail = authentication.getName();
        UserResponse user = adminService.removeUserRole(userId, role, operatorEmail);
        return ResponseEntity.ok(ApiResponse.success("角色移除成功", user));
    }

    /**
     * 獲取已刪除的用戶列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deleted-users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getDeletedUsers() {
        List<UserResponse> deletedUsers = adminService.getDeletedUsers();
        return ResponseEntity.ok(ApiResponse.success("獲取已刪除用戶列表成功", deletedUsers));
    }

    /**
     * 獲取用戶統計信息
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStatistics() {
        Map<String, Object> statistics = adminService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success("獲取統計信息成功", statistics));
    }
}
