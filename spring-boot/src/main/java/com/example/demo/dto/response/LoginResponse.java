package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 登入回應DTO
 * 回傳JWT與用戶資訊
 */
@Data
@Builder
public class LoginResponse {
    private String token;
    private UserResponse user;
}
