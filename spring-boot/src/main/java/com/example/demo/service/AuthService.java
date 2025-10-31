package com.example.demo.service;

import com.example.demo.dto.request.ForgotPasswordRequest;
import com.example.demo.dto.request.SetupTotpRequest;
import com.example.demo.dto.request.VerifyTotpRequest;
import com.example.demo.dto.request.frontend.RegisterRequest;
import com.example.demo.dto.response.TotpSetupResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.enums.UserRole;

/**
 * 認證服務接口
 * 處理註冊、登入等認證業務邏輯
 */
public interface AuthService {

    /**
     * 前台用戶註冊
     */
    UserResponse register(RegisterRequest request);

    /**
     * 管理員創建用戶（可指定角色）
     */
    UserResponse createUser(RegisterRequest request, UserRole role, String createdBy);

    /**
     * 用戶登入
     */
    LoginResponse login(LoginRequest request);

    /**
     * 檢查用戶是否可以登入（帳戶狀態檢查）
     */
    boolean canUserLogin(String email);

    /**
     * 忘記密碼 - 重設密碼
     */
    void forgotPassword(ForgotPasswordRequest request);

    /**
     * 設定 TOTP（Google Authenticator）
     */
    TotpSetupResponse setupTotp(SetupTotpRequest request);

    /**
     * 驗證並啟用 TOTP
     */
    void verifyAndEnableTotp(VerifyTotpRequest request);

    /**
     * 停用 TOTP
     */
    void disableTotp(String email);
}
