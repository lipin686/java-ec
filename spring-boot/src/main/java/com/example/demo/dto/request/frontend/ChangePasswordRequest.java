package com.example.demo.dto.request.frontend;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 前台用戶修改密碼請求DTO
 */
@Data
public class ChangePasswordRequest {

    @NotBlank(message = "當前密碼不能為空")
    private String currentPassword;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "新密碼長度至少6位")
    private String newPassword;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;
}
