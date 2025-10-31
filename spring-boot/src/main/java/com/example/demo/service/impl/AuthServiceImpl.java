package com.example.demo.service.impl;

import com.example.demo.dto.request.ForgotPasswordRequest;
import com.example.demo.dto.request.LoginRequest;
import com.example.demo.dto.request.SetupTotpRequest;
import com.example.demo.dto.request.VerifyTotpRequest;
import com.example.demo.dto.request.frontend.RegisterRequest;
import com.example.demo.dto.response.LoginResponse;
import com.example.demo.dto.response.TotpSetupResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import com.example.demo.exception.AccountStatusException;
import com.example.demo.exception.CustomException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.TotpService;
import com.example.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 認證服務實現類
 * 處理用戶註冊、登入的具體業務邏輯
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TotpService totpService;

    @Override
    public UserResponse register(RegisterRequest request) {
        // 1. 檢查郵箱是否已經存在（包括軟刪除的用戶）
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new CustomException("郵箱已存在");
        }

        // 2. 創建新的前台用戶
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(UserRole.USER))  // 預設為前台用戶
                .authProvider("local")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .deleted(false)
                .build();

        // 3. 保存到數據庫
        User savedUser = userRepository.save(user);

        // 4. 轉換為響應DTO
        return convertToUserResponse(savedUser);
    }

    @Override
    public UserResponse createUser(RegisterRequest request, UserRole role, String createdBy) {
        // 1. 檢查郵箱是否已經存在
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new CustomException("郵箱已存在");
        }

        // 2. 創建用戶（管理員可以指定角色）
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(role))
                .authProvider("local")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .deleted(false)
                .createdBy(createdBy)
                .build();

        // 3. 保存到數據庫
        User savedUser = userRepository.save(user);

        // 4. 轉換為響應DTO
        return convertToUserResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查詢啟用且未刪除的用戶
        User user = userRepository.findByEmailAndEnabledAndDeletedFalse(request.getEmail(), true)
                .orElseThrow(() -> new UserNotFoundException("用戶不存在或已被停用"));

        // 2. 驗證密碼
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("密碼錯誤");
        }

        // 3. 檢查帳戶狀態
        validateUserAccountStatus(user);

        // 4. 更新最後登入時間
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 5. 產生 JWT token
        String token = jwtUtil.generateToken(user.getEmail());

        // 6. 回傳 LoginResponse
        return LoginResponse.builder()
                .token(token)
                .user(convertToUserResponse(user))
                .build();
    }

    @Override
    public boolean canUserLogin(String email) {
        return userRepository.findByEmailAndEnabledAndDeletedFalse(email, true)
                .map(user -> user.getAccountNonExpired() &&
                           user.getAccountNonLocked() &&
                           user.getCredentialsNonExpired())
                .orElse(false);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        // 1. 查詢用戶是否存在且未被刪除
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("該信箱不存在或用戶已被刪除"));

        // 2. 檢查帳戶是否啟用
        if (!user.getEnabled()) {
            throw new AccountStatusException("帳戶已被停用，無法重設密碼");
        }

        // 3. 檢查帳戶是否被鎖定
        if (!user.getAccountNonLocked()) {
            throw new AccountStatusException("帳戶已被鎖定，請聯繫管理員");
        }

        // 4. 檢查是否已啟用 TOTP
        if (user.getTotpEnabled() && user.getTotpSecret() != null) {
            // 如果啟用了 TOTP，必須驗證 TOTP 代碼
            if (request.getTotpCode() == null) {
                throw new BadCredentialsException("該帳戶已啟用雙因素驗證，請提供 TOTP 驗證碼");
            }
            if (!totpService.verifyCode(user.getTotpSecret(), request.getTotpCode())) {
                throw new BadCredentialsException("TOTP 驗證碼錯誤");
            }
        }
        // 如果沒有啟用 TOTP，可以直接重設密碼（不需要額外驗證）

        // 5. 更新密碼
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 6. 重設密碼後，更新相關狀態
        user.setCredentialsNonExpired(true);
        user.setUpdatedAt(LocalDateTime.now());

        // 7. 保存到數據庫
        userRepository.save(user);
    }

    @Override
    public TotpSetupResponse setupTotp(SetupTotpRequest request) {
        // 1. 查詢用戶是否存在
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("用戶不存在"));

        // 2. 檢查帳戶狀態
        if (!user.getEnabled()) {
            throw new AccountStatusException("帳戶已被停用");
        }

        // 3. 生成 TOTP 密鑰
        String secret = totpService.generateSecret();

        // 4. 暫存 secret 到資料庫（但還不啟用 TOTP）
        user.setTotpSecret(secret);
        user.setTotpEnabled(false); // 設定為 false，等驗證成功後才啟用
        userRepository.save(user);

        // 5. 生成 QR Code URL
        String issuer = "Demo App"; // 你可以改成你的應用名稱
        String qrCodeUrl = totpService.generateQrCodeUrl(request.getEmail(), secret, issuer);

        // 6. 回傳設定資訊
        return TotpSetupResponse.builder()
                .secret(secret)
                .qrCodeUrl(qrCodeUrl)
                .manualEntryKey(secret)
                .issuer(issuer)
                .build();
    }

    @Override
    public void verifyAndEnableTotp(VerifyTotpRequest request) {
        // 1. 查詢用戶是否存在
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("用戶不存在"));

        // 2. 檢查是否已經設定過 secret（必須先調用 setupTotp）
        if (user.getTotpSecret() == null || user.getTotpSecret().isEmpty()) {
            throw new CustomException("請先設定 TOTP，調用 setup-totp API");
        }

        // 3. 使用資料庫中暫存的 secret 驗證 TOTP 代碼
        if (!totpService.verifyCode(user.getTotpSecret(), request.getCode())) {
            throw new BadCredentialsException("TOTP 驗證碼錯誤");
        }

        // 4. 驗證成功，啟用 TOTP
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    // 新增一個包含 secret 的驗證方法
    public void verifyAndEnableTotpWithSecret(String email, String secret, Integer code) {
        // 1. 查詢用戶是否存在
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("用戶不存在"));

        // 2. 驗證 TOTP 代碼
        if (!totpService.verifyCode(secret, code)) {
            throw new BadCredentialsException("TOTP 驗證碼錯誤");
        }

        // 3. 驗證成功，啟用 TOTP
        totpService.enableTotp(email, secret);
    }

    @Override
    public void disableTotp(String email) {
        // 1. 查詢用戶是否存在
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("用戶不存在"));

        // 2. 停用 TOTP
        totpService.disableTotp(email);
    }

    /**
     * 驗證用戶帳戶狀態
     */
    private void validateUserAccountStatus(User user) {
        if (!user.getAccountNonLocked()) {
            throw new AccountStatusException("帳戶已被鎖定");
        }
        if (!user.getAccountNonExpired()) {
            throw new AccountStatusException("帳戶已過期");
        }
        if (!user.getCredentialsNonExpired()) {
            throw new AccountStatusException("密碼已過期，請重設密碼");
        }
    }

    /**
     * 轉換User實體為UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())  // 修改：使用多角色
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
