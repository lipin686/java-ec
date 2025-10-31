package com.example.demo.controller.frontend;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.frontend.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 前台用戶 Controller
 * 處理一般用戶的HTTP請求
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserController {

    private final UserService userService;

    /**
     * 獲取當前登入用戶資訊
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("獲取用戶資訊成功", user));
    }

    /**
     * 獲取指定用戶資訊（只能查看自己的，管理員可以查看所有）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id, Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.getUserById(id, email);
        return ResponseEntity.ok(ApiResponse.success("獲取用戶資訊成功", user));
    }

    /**
     * 更新個人資訊
     */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody UserResponse updateRequest,
            Authentication authentication) {
        String email = authentication.getName();
        UserResponse user = userService.updateUserProfile(updateRequest, email);
        return ResponseEntity.ok(ApiResponse.success("更新用戶資訊成功", user));
    }

    /**
     * 獲取用戶統計信息
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Object>> getUserStats(Authentication authentication) {
        String email = authentication.getName();
        Object stats = userService.getUserStats(email);
        return ResponseEntity.ok(ApiResponse.success("獲取統計信息成功", stats));
    }
}
