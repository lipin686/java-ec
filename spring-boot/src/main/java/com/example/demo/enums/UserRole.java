package com.example.demo.enums;

/**
 * 使用者角色枚舉
 * ADMIN: 後台管理員
 * USER: 前台一般使用者
 */
public enum UserRole {
    ADMIN("ROLE_ADMIN", "後台管理員"),
    USER("ROLE_USER", "前台使用者");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
