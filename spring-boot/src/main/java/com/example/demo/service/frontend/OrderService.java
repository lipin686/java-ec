package com.example.demo.service.frontend;

import com.example.demo.dto.request.frontend.CreateOrderRequest;
import com.example.demo.dto.response.frontend.OrderResponse;
import com.example.demo.enums.OrderStatus;

import java.util.List;

/**
 * 訂單服務接口
 */
public interface OrderService {

    /**
     * 創建訂單（從購物車已勾選的項目）
     * @param userId 用戶ID
     * @param request 訂單請求
     * @return 訂單響應
     */
    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    /**
     * 根據訂單ID獲取訂單詳情
     * @param userId 用戶ID
     * @param orderId 訂單ID
     * @return 訂單響應
     */
    OrderResponse getOrderById(Long userId, Long orderId);

    /**
     * 根據訂單編號獲取訂單詳情
     * @param userId 用戶ID
     * @param orderNumber 訂單編號
     * @return 訂單響應
     */
    OrderResponse getOrderByOrderNumber(Long userId, String orderNumber);

    /**
     * 獲取用戶的訂單列表
     * @param userId 用戶ID
     * @param status 訂單狀態（可選，null 則查詢全部）
     * @return 訂單列表
     */
    List<OrderResponse> getUserOrders(Long userId, OrderStatus status);

    /**
     * 取消訂單
     * @param userId 用戶ID
     * @param orderId 訂單ID
     * @return 訂單響應
     */
    OrderResponse cancelOrder(Long userId, Long orderId);


    /**
     * 獲取用戶訂單數量
     * @param userId 用戶ID
     * @param status 訂單狀態（可選，null 則查詢全部）
     * @return 訂單數量
     */
    Long getUserOrderCount(Long userId, OrderStatus status);
}

