package com.example.demo.service.backend;

import com.example.demo.dto.request.backend.CreateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.enums.UserRole;

import java.util.List;
import java.util.Map;

/**
 * 管理員服務接口
 * 處理後台管理相關業務邏輯
 */
public interface AdminService {

    /**
     * 創建管理員帳號
     */
    UserResponse createAdmin(CreateUserRequest request, String createdBy);

    /**
     * 創建前台用戶帳號
     */
    UserResponse createUser(CreateUserRequest request, String createdBy);

    /**
     * 獲取所有用戶
     */
    List<UserResponse> getAllUsers();

    /**
     * 根據角色獲取用戶
     */
    List<UserResponse> getUsersByRole(UserRole role);

    /**
     * 切換用戶啟用狀態
     */
    UserResponse toggleUserStatus(Long userId, String operatorEmail);

    /**
     * 軟刪除用戶
     */
    void deleteUser(Long userId, String deletedBy);

    /**
     * 恢復已刪除的用戶
     */
    UserResponse restoreUser(Long userId, String restoredBy);

    /**
     * 獲取用戶統計信息
     */
    Map<String, Object> getUserStatistics();

    /**
     * 獲取已刪除的用戶列表
     */
    List<UserResponse> getDeletedUsers();

    /**
     * 為用戶添加角色
     */
    UserResponse addUserRole(Long userId, UserRole role, String operatorEmail);

    /**
     * 移除用戶角色
     */
    UserResponse removeUserRole(Long userId, UserRole role, String operatorEmail);
}
