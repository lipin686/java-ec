package com.example.demo.dto.response;

import com.example.demo.enums.UserRole;
import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用戶響應DTO
 * 用於返回給前端的用戶數據格式
 */
@Data
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Set<UserRole> roles;  // 改為支援多角色
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 為了向後相容，提供一個方法返回主要角色
    public UserRole getRole() {
        if (roles != null && !roles.isEmpty()) {
            // 如果有 ADMIN 角色，優先返回 ADMIN
            if (roles.contains(UserRole.ADMIN)) {
                return UserRole.ADMIN;
            }
            // 否則返回第一個角色
            return roles.iterator().next();
        }
        return UserRole.USER;  // 預設返回 USER
    }
}
