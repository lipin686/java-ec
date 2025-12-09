package com.example.demo.controller.frontend;

import com.example.demo.dto.request.AddToCartRequest;
import com.example.demo.dto.request.UpdateCartItemRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.frontend.CartResponse;
import com.example.demo.security.CustomUserDetails;
import com.example.demo.service.frontend.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 購物車控制器
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class CartController {

    private final CartService cartService;

    /**
     * 獲取當前用戶的購物車
     */
    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.getCart(userId);
        return ApiResponse.success("獲取購物車成功", cart);
    }

    /**
     * 添加商品到購物車
     */
    @PostMapping("/items")
    public ApiResponse<CartResponse> addToCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.addToCart(userId, request);
        return ApiResponse.success("添加商品成功", cart);
    }

    /**
     * 更新購物車項目數量
     */
    @PutMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.updateCartItem(userId, cartItemId, request);
        return ApiResponse.success("更新購物車項目成功", cart);
    }

    /**
     * 刪除購物車項目
     */
    @DeleteMapping("/items/{cartItemId}")
    public ApiResponse<CartResponse> removeCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.removeCartItem(userId, cartItemId);
        return ApiResponse.success("刪除購物車項目成功", cart);
    }

    /**
     * 切換購物車項目的勾選狀態
     */
    @PatchMapping("/items/{cartItemId}/toggle")
    public ApiResponse<CartResponse> toggleCartItemChecked(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.toggleCartItemChecked(userId, cartItemId);
        return ApiResponse.success("切換勾選狀態成功", cart);
    }

    /**
     * 全選/取消全選購物車項目
     */
    @PatchMapping("/items/toggle-all")
    public ApiResponse<CartResponse> toggleAllCartItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Boolean checked) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.toggleAllCartItems(userId, checked);
        return ApiResponse.success("批量切換勾選狀態成功", cart);
    }

    /**
     * 清空購物車
     */
    @DeleteMapping
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        cartService.clearCart(userId);
        return ApiResponse.success("清空購物車成功", null);
    }

    /**
     * 批量刪除購物車項目
     */
    @DeleteMapping("/items/batch")
    public ApiResponse<CartResponse> batchRemoveCartItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody List<Long> cartItemIds) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.batchRemoveCartItems(userId, cartItemIds);
        return ApiResponse.success("批量刪除成功", cart);
    }

    /**
     * 刪除已勾選的購物車項目
     */
    @DeleteMapping("/items/checked")
    public ApiResponse<CartResponse> removeCheckedItems(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.removeCheckedItems(userId);
        return ApiResponse.success("刪除已勾選項目成功", cart);
    }

    /**
     * 獲取已勾選的購物車項目（用於結帳）
     */
    @GetMapping("/checked")
    public ApiResponse<CartResponse> getCheckedItems(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.getCheckedItems(userId);
        return ApiResponse.success("獲取已勾選項目成功", cart);
    }

    /**
     * 獲取購物車項目數量（輕量級接口，用於顯示徽章）
     */
    @GetMapping("/count")
    public ApiResponse<Integer> getCartItemCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        Integer count = cartService.getCartItemCount(userId);
        return ApiResponse.success("獲取購物車數量成功", count);
    }

    /**
     * 驗證購物車項目（檢查庫存和商品狀態）
     */
    @PostMapping("/validate")
    public ApiResponse<CartResponse> validateCart(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        CartResponse cart = cartService.validateCart(userId);
        return ApiResponse.success("購物車驗證完成", cart);
    }
}
