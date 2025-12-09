-- 訂單主表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵',
    order_number VARCHAR(50) NOT NULL UNIQUE COMMENT '訂單編號',
    user_id BIGINT NOT NULL COMMENT '會員ID',
    total_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '訂單總金額',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '訂單狀態',
    receiver_name VARCHAR(100) NOT NULL COMMENT '收件人姓名',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '收件人電話',
    receiver_address VARCHAR(500) NOT NULL COMMENT '收件地址',
    remark TEXT COMMENT '訂單備註',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_order_number (order_number),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單主表';

-- 訂單明細表
CREATE TABLE order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主鍵',
    order_id BIGINT NOT NULL COMMENT '訂單ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名稱（快照）',
    product_image VARCHAR(500) COMMENT '商品圖片（快照）',
    price DECIMAL(10, 2) NOT NULL COMMENT '商品單價（快照）',
    quantity INT NOT NULL DEFAULT 1 COMMENT '購買數量',
    subtotal DECIMAL(10, 2) NOT NULL COMMENT '小計金額',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單明細表';

