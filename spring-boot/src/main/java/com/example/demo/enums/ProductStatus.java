package com.example.demo.enums;

public enum ProductStatus {
    OPEN,    // 正常顯示
    CLOSED,  // 完全關閉
    HIDDEN;  // 隱藏（前台不可見，後台可查詢）

    /**
     * 安全解析字串為 ProductStatus，無效時回傳 null。
     */
    public static ProductStatus parseOrNull(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return ProductStatus.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }
}
