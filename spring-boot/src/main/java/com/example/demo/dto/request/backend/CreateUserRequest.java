package com.example.demo.dto.request.backend;

import com.example.demo.enums.UserRole;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

/**
 * 後台創建用戶請求DTO
 * 管理員用於創建新用戶（可指定角色）
 */
@Data
public class CreateUserRequest {

    @NotBlank(message = "姓名不能為空")
    private String name;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少6位")
    private String password;

    @NotNull(message = "用戶角色不能為空")
    private UserRole role;

    /**
     * 是否立即啟用帳戶，默認為true
     */
    private Boolean enabled = true;
}
