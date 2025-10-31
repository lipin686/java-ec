package com.example.demo.service.frontend.impl;

import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.frontend.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 前台用戶服務實現類
 * 實現前台用戶相關的業務邏輯
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse getUserById(Long id, String currentUserEmail) {
        User user = userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        User currentUser = userRepository.findByEmailAndDeletedFalse(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("當前用戶不存在"));

        // 只允許查看自己的資訊，除非是管理員
        if (!currentUser.isAdmin() && !user.getEmail().equals(currentUserEmail)) {
            throw new RuntimeException("無權限查看其他用戶資訊");
        }

        return convertToUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        return convertToUserResponse(user);
    }

    @Override
    public UserResponse updateUserProfile(UserResponse updateRequest, String currentUserEmail) {
        User currentUser = userRepository.findByEmailAndDeletedFalse(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("當前用戶不存在"));

        // 只允許更新自己的基本資訊（除了角色等敏感欄位）
        if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
            currentUser.setName(updateRequest.getName().trim());
        }

        User updatedUser = userRepository.save(currentUser);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public boolean hasRole(Long userId, UserRole role) {
        return userRepository.userHasRole(userId, role);
    }

    @Override
    public Object getUserStats(String currentUserEmail) {
        User user = userRepository.findByEmailAndDeletedFalse(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("當前用戶不存在"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("userId", user.getId());
        stats.put("loginCount", 0); // 可以後續添加登入次數統計
        stats.put("lastLoginAt", user.getLastLoginAt());
        stats.put("memberSince", user.getCreatedAt());
        stats.put("points", user.getPoints());

        return stats;
    }

    /**
     * 轉換User實體為UserResponse DTO
     */
    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())  // 修改：使用多角色
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
