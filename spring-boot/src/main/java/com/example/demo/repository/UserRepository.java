package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * User Repository
 * 支援前台後台使用者的數據訪問層
 * 優先使用Spring Data JPA方法命名，複雜查詢才使用JPQL
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // === 基本查詢（Spring Data JPA方法命名） ===

    /**
     * 根據郵箱查找用戶
     */
    Optional<User> findByEmail(String email);

    /**
     * 根據郵箱查找未刪除的用戶
     */
    Optional<User> findByEmailAndDeletedFalse(String email);

    /**
     * 根據郵箱和啟用狀態查找未刪除用戶
     */
    Optional<User> findByEmailAndEnabledAndDeletedFalse(String email, Boolean enabled);

    /**
     * 檢查郵箱是否存在（未刪除）
     */
    boolean existsByEmailAndDeletedFalse(String email);

    // === 軟刪除相關查詢（Spring Data JPA方法命名） ===

    /**
     * 查找所有未刪除的用戶
     */
    List<User> findByDeletedFalse();

    /**
     * 查找所有已刪除的用戶
     */
    List<User> findByDeletedTrue();

    /**
     * 根據刪除者查找被刪除的用戶
     */
    List<User> findByDeletedTrueAndDeletedBy(String deletedBy);

    /**
     * 查找在指定時間範圍內被刪除的用戶
     */
    List<User> findByDeletedTrueAndDeletedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // === 帳戶狀態查詢（Spring Data JPA方法命名） ===

    /**
     * 查找啟用且未刪除的用戶
     */
    List<User> findByEnabledTrueAndDeletedFalse();

    /**
     * 查找停用且未刪除的用戶
     */
    List<User> findByEnabledFalseAndDeletedFalse();

    /**
     * 查找被鎖定的用戶
     */
    List<User> findByAccountNonLockedFalseAndDeletedFalse();

    // === 第三方登入相關查詢（Spring Data JPA方法命名） ===

    /**
     * 根據第三方提供者和providerId查找用戶
     */
    Optional<User> findByAuthProviderAndProviderIdAndDeletedFalse(String authProvider, String providerId);

    /**
     * 根據第三方提供者查找用戶
     */
    List<User> findByAuthProviderAndDeletedFalse(String authProvider);

    // === 統計查詢（Spring Data JPA方法命名） ===

    /**
     * 統計未刪除用戶總數
     */
    long countByDeletedFalse();

    /**
     * 統計啟用用戶數
     */
    long countByEnabledTrueAndDeletedFalse();

    // === 時間範圍查詢（Spring Data JPA方法命名） ===

    /**
     * 查找在指定時間後創建的用戶
     */
    List<User> findByCreatedAtAfterAndDeletedFalse(LocalDateTime date);

    /**
     * 查找在指定時間後最後登入的用戶
     */
    List<User> findByLastLoginAtAfterAndDeletedFalse(LocalDateTime date);

    // === 複雜角色查詢（必須使用JPQL，因為涉及集合關聯） ===

    /**
     * 查找擁有指定角色的所有用戶
     * 注意：由於roles是Set集合，無法用方法命名實現
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.deleted = false")
    List<User> findByRolesContaining(@Param("role") UserRole role);

    /**
     * 查找擁有指定角色且啟用的用戶
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role AND u.enabled = true AND u.deleted = false")
    List<User> findActiveUsersByRole(@Param("role") UserRole role);

    /**
     * 檢查用戶是否擁有指定角色
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.roles r WHERE u.id = :userId AND r = :role")
    boolean userHasRole(@Param("userId") Long userId, @Param("role") UserRole role);

    /**
     * 統計擁有指定角色的用戶數
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = :role AND u.deleted = false")
    long countByRole(@Param("role") UserRole role);

    // === 業務特定查詢（使用JPQL提高可讀性） ===

    /**
     * 查找所有管理員（未刪除）
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = com.example.demo.enums.UserRole.ADMIN AND u.deleted = false")
    List<User> findAllAdmins();

    /**
     * 查找所有前台用戶（未刪除）
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = com.example.demo.enums.UserRole.USER AND u.deleted = false")
    List<User> findAllFrontendUsers();

    /**
     * 統計管理員數量
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = com.example.demo.enums.UserRole.ADMIN AND u.deleted = false")
    long countAdmins();

    /**
     * 統計前台用戶數量
     */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = com.example.demo.enums.UserRole.USER AND u.deleted = false")
    long countFrontendUsers();

    /**
     * 查找長時間未登入的用戶
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :date AND u.deleted = false")
    List<User> findInactiveUsers(@Param("date") LocalDateTime date);

    /**
     * 查找所有第三方登入用戶
     */
    @Query("SELECT u FROM User u WHERE u.authProvider != 'local' AND u.deleted = false")
    List<User> findThirdPartyUsers();

    /**
     * 檢查郵箱是否已被其他未刪除用戶使用（用於更新驗證）
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.id != :userId AND u.deleted = false")
    boolean existsByEmailAndNotId(@Param("email") String email, @Param("userId") Long userId);
}
