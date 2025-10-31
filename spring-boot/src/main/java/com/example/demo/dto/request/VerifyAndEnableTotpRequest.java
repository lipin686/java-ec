package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 驗證並啟用 TOTP 請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyAndEnableTotpRequest {

    @NotBlank(message = "信箱不能為空")
    @Email(message = "信箱格式不正確")
    private String email;

    @NotBlank(message = "TOTP 密鑰不能為空")
    private String secret;

    @NotNull(message = "驗證碼不能為空")
    private Integer code;
}
