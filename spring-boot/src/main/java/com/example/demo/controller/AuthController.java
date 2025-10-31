package com.example.demo.controller;

import com.example.demo.dto.request.ForgotPasswordRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.SetupTotpRequest;
import com.example.demo.dto.request.VerifyTotpRequest;
import com.example.demo.dto.request.frontend.RegisterRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.TotpSetupResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.AccountStatusException;
import com.example.demo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認證控制器
 * 處理註冊、登入等認證相關功能
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 前台用戶註冊
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("註冊成功", user));
    }

    /**
     * 用戶登入（前台用戶專用）
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        // 檢查是否具有前台用戶角色
        UserResponse user = response.getUser();
        if (!user.getRoles().contains(UserRole.USER)) {
            throw new AccountStatusException("此帳號無法使用前台服務");
        }

        return ResponseEntity.ok(ApiResponse.success("登入成功", response));
    }

    /**
     * 檢查用戶登入狀態
     */
    @GetMapping("/check-login/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkCanLogin(@PathVariable String email) {
        boolean canLogin = authService.canUserLogin(email);
        return ResponseEntity.ok(ApiResponse.success("檢查完成", canLogin));
    }

    /**
     * 忘記密碼 - 重設密碼
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("密碼重設成功", "您的密碼已成功更新，請使用新密碼登入"));
    }

    /**
     * 設定 TOTP（Google Authenticator）
     */
    @PostMapping("/setup-totp")
    public ResponseEntity<ApiResponse<TotpSetupResponse>> setupTotp(@Valid @RequestBody SetupTotpRequest request) {
        TotpSetupResponse response = authService.setupTotp(request);
        return ResponseEntity.ok(ApiResponse.success("TOTP 設定資訊已生成", response));
    }

    /**
     * 驗證並啟用 TOTP
     */
    @PostMapping("/verify-enable-totp")
    public ResponseEntity<ApiResponse<String>> verifyAndEnableTotp(@Valid @RequestBody VerifyTotpRequest request) {
        authService.verifyAndEnableTotp(request);
        return ResponseEntity.ok(ApiResponse.success("TOTP 已成功啟用", "您的帳戶現在已啟用雙因素驗證"));
    }

    /**
     * 停用 TOTP
     */
    @PostMapping("/disable-totp")
    public ResponseEntity<ApiResponse<String>> disableTotp(@RequestParam String email) {
        authService.disableTotp(email);
        return ResponseEntity.ok(ApiResponse.success("TOTP 已停用", "您的帳戶已關閉雙因素驗證"));
    }

    /**
     * 登出（前端處理JWT即可，這裡提供統一響應）
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("登出成功", "請清除本地JWT Token"));
    }
}
