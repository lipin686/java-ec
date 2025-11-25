-- 用戶主表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '郵箱',
    password VARCHAR(255) NOT NULL COMMENT '密碼(加密)',

    -- 帳戶狀態
    enabled BOOLEAN NOT NULL DEFAULT TRUE COMMENT '帳戶是否啟用',
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '帳戶是否未過期',
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE COMMENT '帳戶是否未鎖定',
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE COMMENT '密碼是否未過期',

    -- 前台使用者專用欄位
    points DECIMAL(19, 2) DEFAULT 0.00 COMMENT '點數餘額',
    phone VARCHAR(20) COMMENT '手機號碼',
    last_login_at TIMESTAMP NULL COMMENT '最後登入時間',

    -- 軟刪除欄位
    deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '軟刪除標記',
    deleted_at TIMESTAMP NULL COMMENT '刪除時間',
    deleted_by VARCHAR(100) COMMENT '刪除者',

    -- 第三方登入支援
    auth_provider VARCHAR(20) DEFAULT 'local' COMMENT '認證提供者',
    provider_id VARCHAR(100) COMMENT '第三方平台用戶ID',

    -- TOTP (2FA) 相關
    totp_secret VARCHAR(255) COMMENT 'TOTP密鑰',
    totp_enabled BOOLEAN DEFAULT FALSE COMMENT '是否啟用TOTP',

    -- 審計欄位
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    created_by VARCHAR(100) COMMENT '創建者',

    -- 索引
    INDEX idx_email (email),
    INDEX idx_deleted (deleted),
    INDEX idx_auth_provider (auth_provider)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用戶表';

-- 用戶角色關聯表 (多對多)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL COMMENT '用戶ID',
    role VARCHAR(20) NOT NULL COMMENT '角色',
    PRIMARY KEY (user_id, role),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用戶角色關聯表';

