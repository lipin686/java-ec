package com.example.demo.service;

/**
 * TOTP (Time-based One-Time Password) 服務接口
 * 用於處理 Google Authenticator 相關功能
 */
public interface TotpService {

    /**
     * 生成 TOTP 密鑰
     */
    String generateSecret();

    /**
     * 生成 QR Code URL，用戶可掃描此 URL 設定 Google Authenticator
     */
    String generateQrCodeUrl(String email, String secret, String issuer);

    /**
     * 驗證 TOTP 代碼
     */
    boolean verifyCode(String secret, int code);

    /**
     * 為用戶啟用 TOTP
     */
    void enableTotp(String email, String secret);

    /**
     * 為用戶停用 TOTP
     */
    void disableTotp(String email);
}
