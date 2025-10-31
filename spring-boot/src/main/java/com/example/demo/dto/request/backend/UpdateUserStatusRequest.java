package com.example.demo.dto.request.backend;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 更新用戶狀態請求DTO
 * 管理員用於更新用戶狀態、角色等
 */
@Data
public class UpdateUserStatusRequest {

    /**
     * 是否啟用帳戶
     */
    private Boolean enabled;

    /**
     * 是否鎖定帳戶
     */
    private Boolean accountNonLocked;

    /**
     * 操作原因（可選）
     */
    private String reason;
}
