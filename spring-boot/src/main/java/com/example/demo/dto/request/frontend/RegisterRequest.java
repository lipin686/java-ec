package com.example.demo.dto.request.frontend;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * 前台用戶註冊請求DTO
 * 用於前台用戶註冊
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "姓名不能為空")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String password;
}
