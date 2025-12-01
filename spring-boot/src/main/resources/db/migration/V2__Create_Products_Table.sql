-- 商品表
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵',
    name VARCHAR(200) NOT NULL COMMENT '商品名稱',
    description TEXT COMMENT '商品描述',
    price DECIMAL(19, 2) NOT NULL COMMENT '商品價格',
    stock INT NOT NULL DEFAULT 0 COMMENT '庫存數量',
    image_url VARCHAR(500) COMMENT '商品圖片URL',

    -- 商品狀態
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT '商品狀態: OPEN-開啟, CLOSE-關閉',
    start_at TIMESTAMP NULL COMMENT '上架開始時間',
    end_at TIMESTAMP NULL COMMENT '上架結束時間',

    -- 商品編號
    product_no VARCHAR(50) NOT NULL UNIQUE COMMENT '商品編號',

    -- 軟刪除
    deleted_at TIMESTAMP NULL COMMENT '刪除時間',

    -- 審計欄位
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '創建時間',
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',

    -- 索引
    INDEX idx_product_no (product_no),
    INDEX idx_status (status),
    INDEX idx_deleted_at (deleted_at),
    INDEX idx_price (price)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

