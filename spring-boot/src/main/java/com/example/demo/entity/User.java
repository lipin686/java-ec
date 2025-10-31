package com.example.demo.entity;

import com.example.demo.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * User實體 - 支援前台後台使用者
 * 使用單表策略，透過角色欄位區分前台後台使用者
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;  // 密碼欄位，會存儲加密後的密碼

    // === 角色相關欄位 ===
    @ElementCollection(targetClass = UserRole.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", length = 20)  // 增加列長度以避免截斷
    @Builder.Default
    private Set<UserRole> roles = Set.of(UserRole.USER);  // 支援多角色，預設為前台使用者

    // === 帳戶狀態欄位 ===
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;  // 帳戶是否啟用

    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonExpired = true;  // 帳戶是否未過期

    @Column(nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;  // 帳戶是否未鎖定

    @Column(nullable = false)
    @Builder.Default
    private Boolean credentialsNonExpired = true;  // 密碼是否未過期

    // === 前台使用者專用欄位 ===
    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal points = BigDecimal.ZERO;  // 點數餘額

    private String phone;  // 手機號碼

    private LocalDateTime lastLoginAt;  // 最後登入時間

    // === 軟刪除欄位 ===
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;  // 軟刪除標記

    private LocalDateTime deletedAt;  // 刪除時間

    private String deletedBy;  // 刪除者（記錄是哪個管理員刪除的）

    // === 第三方登入支援欄位 ===
    @Column(name = "auth_provider")
    @Builder.Default
    private String authProvider = "local";  // 認證提供者：local, google, facebook 等

    @Column(name = "provider_id")
    private String providerId;  // 第三方平台的用戶ID

    // === TOTP (2FA) 相關欄位 ===
    @Column(name = "totp_secret")
    private String totpSecret;  // TOTP 密鑰

    @Column(name = "totp_enabled")
    @Builder.Default
    private Boolean totpEnabled = false;  // 是否啟用 TOTP

    // === 審計欄位 ===
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 創建時間

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;  // 更新時間

    @Column(name = "created_by")
    private String createdBy;  // 創建者（用於記錄是哪個管理員創建的）

    // === 業務邏輯方法 ===

    /**
     * 檢查是否為管理員
     */
    public boolean isAdmin() {
        return roles.contains(UserRole.ADMIN);
    }

    /**
     * 檢查是否為前台使用者
     */
    public boolean isFrontendUser() {
        return roles.contains(UserRole.USER);
    }

    /**
     * 檢查是否有指定角色
     */
    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    /**
     * 添加角色
     */
    public void addRole(UserRole role) {
        this.roles.add(role);
    }

    /**
     * 移除角色
     */
    public void removeRole(UserRole role) {
        this.roles.remove(role);
    }

    /**
     * 檢查是否為第三方登入用戶
     */
    public boolean isThirdPartyUser() {
        return !"local".equals(authProvider);
    }

    /**
     * 增加點數
     */
    public void addPoints(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.points = this.points.add(amount);
        }
    }

    /**
     * 扣除點數
     */
    public boolean deductPoints(BigDecimal amount) {
        if (amount != null && this.points.compareTo(amount) >= 0) {
            this.points = this.points.subtract(amount);
            return true;
        }
        return false;
    }

    // === 軟刪除相關方法 ===

    /**
     * 軟刪除用戶
     */
    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.enabled = false; // 同時停用帳戶
    }

    /**
     * 恢復軟刪除的用戶
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
        this.enabled = true; // 同時啟用帳戶
    }

    /**
     * 檢查是否已被軟刪除
     */
    public boolean isDeleted() {
        return this.deleted != null && this.deleted;
    }

    /**
     * 檢查是否為有效用戶（未刪除且已啟用）
     */
    public boolean isActive() {
        return !isDeleted() && this.enabled;
    }
}
