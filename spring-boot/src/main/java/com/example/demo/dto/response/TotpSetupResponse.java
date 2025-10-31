package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 設定 TOTP 回應 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TotpSetupResponse {

    private String secret;
    private String qrCodeUrl;
    private String manualEntryKey;
    private String issuer;
}
