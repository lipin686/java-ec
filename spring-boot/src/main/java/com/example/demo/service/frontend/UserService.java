package com.example.demo.service.frontend;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.enums.UserRole;

/**
 * 前台用戶服務接口
 * 定義前台用戶相關的業務邏輯方法
 */
public interface UserService {

    /**
     * 根據ID獲取用戶（權限檢查）
     */
    UserResponse getUserById(Long id, String currentUserEmail);

    /**
     * 根據Email獲取用戶
     */
    UserResponse getUserByEmail(String email);

    /**
     * 更新用戶資訊
     */
    UserResponse updateUserProfile(UserResponse updateRequest, String currentUserEmail);

    /**
     * 檢查用戶是否擁有指定角色
     */
    boolean hasRole(Long userId, UserRole role);

    /**
     * 獲取當前用戶的基本統計信息
     */
    Object getUserStats(String currentUserEmail);
}
