package com.example.demo.controller.frontend;

import com.example.demo.dto.request.frontend.CreateOrderRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.frontend.OrderResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.frontend.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 訂單控制器
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class OrderController {

    private final OrderService orderService;

    /**
     * 創建訂單（從購物車已勾選的項目）
     */
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        Long userId = userDetails.getUserId();
        OrderResponse order = orderService.createOrder(userId, request);
        return ApiResponse.success("訂單創建成功", order);
    }

    /**
     * 獲取當前用戶的訂單列表
     * @param status 訂單狀態（可選），不傳則查詢全部
     */
    @GetMapping
    public ApiResponse<List<OrderResponse>> getUserOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status) {
        Long userId = userDetails.getUserId();
        List<OrderResponse> orders = orderService.getUserOrders(userId, status);
        return ApiResponse.success("獲取訂單列表成功", orders);
    }

    /**
     * 根據訂單ID獲取訂單詳情
     */
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        Long userId = userDetails.getUserId();
        OrderResponse order = orderService.getOrderById(userId, orderId);
        return ApiResponse.success("獲取訂單詳情成功", order);
    }

    /**
     * 根據訂單編號獲取訂單詳情
     */
    @GetMapping("/number/{orderNumber}")
    public ApiResponse<OrderResponse> getOrderByOrderNumber(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String orderNumber) {
        Long userId = userDetails.getUserId();
        OrderResponse order = orderService.getOrderByOrderNumber(userId, orderNumber);
        return ApiResponse.success("獲取訂單詳情成功", order);
    }

    /**
     * 取消訂單
     */
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long orderId) {
        Long userId = userDetails.getUserId();
        OrderResponse order = orderService.cancelOrder(userId, orderId);
        return ApiResponse.success("訂單取消成功", order);
    }


    /**
     * 獲取用戶訂單數量
     * @param status 訂單狀態（可選），不傳則查詢全部訂單數量
     */
    @GetMapping("/count")
    public ApiResponse<Long> getUserOrderCount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status) {
        Long userId = userDetails.getUserId();
        Long count = orderService.getUserOrderCount(userId, status);
        return ApiResponse.success("獲取訂單數量成功", count);
    }
}

