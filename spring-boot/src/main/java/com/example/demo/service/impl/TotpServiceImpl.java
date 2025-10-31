package com.example.demo.service.impl;

import com.example.demo.entity.User;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TotpService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

/**
 * TOTP 服務實現類
 */
@Service
@RequiredArgsConstructor
public class TotpServiceImpl implements TotpService {

    private final UserRepository userRepository;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    @Override
    public String generateSecret() {
        // 生成符合 Google Authenticator 規範的 Base32 編碼密鑰
        SecureRandom random = new SecureRandom();
        byte[] secretBytes = new byte[20]; // 160 bits = 20 bytes
        random.nextBytes(secretBytes);

        // 使用 Apache Commons Codec 的 Base32 編碼
        Base32 base32 = new Base32();
        return base32.encodeToString(secretBytes).replaceAll("=", ""); // 移除填充字符
    }

    @Override
    public String generateQrCodeUrl(String email, String secret, String issuer) {
        try {
            // 構建標準的 otpauth URL，符合 RFC 6238 規範
            String otpAuthUrl = String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer.replaceAll(" ", "%20"),
                email,
                secret,
                issuer.replaceAll(" ", "%20")
            );

            return otpAuthUrl;
        } catch (Exception e) {
            throw new RuntimeException("無法生成 QR Code URL", e);
        }
    }

    @Override
    public boolean verifyCode(String secret, int code) {
        return googleAuthenticator.authorize(secret, code);
    }

    @Override
    public void enableTotp(String email, String secret) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("用戶不存在"));

        user.setTotpSecret(secret);
        user.setTotpEnabled(true);
        userRepository.save(user);
    }

    @Override
    public void disableTotp(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UserNotFoundException("用戶不存在"));

        user.setTotpSecret(null);
        user.setTotpEnabled(false);
        userRepository.save(user);
    }
}
