package com.example.demo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登入請求DTO
 * 用於接收前端登入資料
 */
@Data
public class LoginRequest {
    @NotBlank(message = "郵箱不能為空")
    @Email(message = "郵箱格式不正確")
    private String email;

    @NotBlank(message = "密碼不能為空")
    private String password;
}

