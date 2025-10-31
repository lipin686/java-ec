package com.example.demo.dto.request.frontend;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 前台用戶更新個人資料請求DTO
 */
@Data
public class UpdateProfileRequest {

    @NotBlank(message = "姓名不能為空")
    private String name;

    private String phone;
}
