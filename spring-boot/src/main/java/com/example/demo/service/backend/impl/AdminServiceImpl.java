package com.example.demo.service.backend.impl;

import com.example.demo.dto.request.backend.CreateUserRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.backend.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理員服務實現類
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createAdmin(CreateUserRequest request, String createdBy) {
        // 檢查郵箱是否已存在
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new RuntimeException("郵箱已存在");
        }

        // 驗證創建者權限
        validateAdminOperator(createdBy);

        // 創建管理員用戶
        User admin = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(UserRole.ADMIN))
                .authProvider("local")
                .enabled(request.getEnabled())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .deleted(false)
                .createdBy(createdBy)
                .build();

        User savedAdmin = userRepository.save(admin);
        return convertToUserResponse(savedAdmin);
    }

    @Override
    public UserResponse createUser(CreateUserRequest request, String createdBy) {
        // 檢查郵箱是否已存在
        if (userRepository.existsByEmailAndDeletedFalse(request.getEmail())) {
            throw new RuntimeException("郵箱已存在");
        }

        // 驗證創建者權限
        validateAdminOperator(createdBy);

        // 創建前台用戶
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(UserRole.USER))
                .authProvider("local")
                .enabled(request.getEnabled())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .deleted(false)
                .createdBy(createdBy)
                .build();

        User savedUser = userRepository.save(user);
        return convertToUserResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findByDeletedFalse().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userRepository.findByRolesContaining(role).stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse toggleUserStatus(Long userId, String operatorEmail) {
        // 驗證操作者權限
        validateAdminOperator(operatorEmail);

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        // 防止停用自己
        if (user.getEmail().equals(operatorEmail)) {
            throw new RuntimeException("不能停用自己的帳戶");
        }

        user.setEnabled(!user.getEnabled());

        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(Long userId, String deletedBy) {
        // 驗證操作者權限
        validateAdminOperator(deletedBy);

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        // 防止刪除自己
        if (user.getEmail().equals(deletedBy)) {
            throw new RuntimeException("不能刪除自己的帳戶");
        }

        user.softDelete(deletedBy);
        userRepository.save(user);
    }

    @Override
    public UserResponse restoreUser(Long userId, String restoredBy) {
        // 驗證操作者權限
        validateAdminOperator(restoredBy);

        User user = userRepository.findById(userId)
                .filter(User::isDeleted)
                .orElseThrow(() -> new RuntimeException("用戶不存在或未被刪除"));

        user.restore();
        User restoredUser = userRepository.save(user);
        return convertToUserResponse(restoredUser);
    }

    @Override
    public Map<String, Object> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 總用戶數（包括已刪除）
        long totalUsers = userRepository.count();
        statistics.put("totalUsers", totalUsers);

        // 有效用戶數
        long activeUsers = userRepository.countByDeletedFalse();
        statistics.put("activeUsers", activeUsers);

        // 管理員數量
        long adminCount = userRepository.countAdmins();
        statistics.put("adminCount", adminCount);

        // 前台用戶數量
        long userCount = userRepository.countFrontendUsers();
        statistics.put("userCount", userCount);

        // 啟用用戶數量
        long enabledUsers = userRepository.countByEnabledTrueAndDeletedFalse();
        statistics.put("enabledUsers", enabledUsers);

        // 停用用戶數量
        long disabledUsers = activeUsers - enabledUsers;
        statistics.put("disabledUsers", disabledUsers);

        // 已刪除用戶數量
        long deletedUsers = totalUsers - activeUsers;
        statistics.put("deletedUsers", deletedUsers);

        return statistics;
    }

    @Override
    public List<UserResponse> getDeletedUsers() {
        return userRepository.findByDeletedTrue().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse addUserRole(Long userId, UserRole role, String operatorEmail) {
        // 驗證操作者權限
        validateAdminOperator(operatorEmail);

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        user.addRole(role);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    @Override
    public UserResponse removeUserRole(Long userId, UserRole role, String operatorEmail) {
        // 驗證操作者權限
        validateAdminOperator(operatorEmail);

        User user = userRepository.findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        // 防止移除自己的管理員角色
        if (user.getEmail().equals(operatorEmail) && role == UserRole.ADMIN) {
            throw new RuntimeException("不能移除自己的管理員角色");
        }

        user.removeRole(role);
        User updatedUser = userRepository.save(user);
        return convertToUserResponse(updatedUser);
    }

    /**
     * 驗證管理員操作者權限
     */
    private void validateAdminOperator(String operatorEmail) {
        User operator = userRepository.findByEmailAndDeletedFalse(operatorEmail)
                .orElseThrow(() -> new RuntimeException("操作者不存在"));

        if (!operator.isAdmin()) {
            throw new RuntimeException("無管理員權限");
        }

        if (!operator.getEnabled()) {
            throw new RuntimeException("操作者帳戶已被停用");
        }
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

