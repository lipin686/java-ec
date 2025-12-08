package com.example.demo.enums;

import lombok.Getter;

/**
 * 訂單狀態枚舉
 */
@Getter
public enum OrderStatus {
    PENDING("待處理"),
    CONFIRMED("已確認"),
    PROCESSING("處理中"),
    SHIPPED("已出貨"),
    DELIVERED("已送達"),
    COMPLETED("已完成"),
    CANCELLED("已取消");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}

