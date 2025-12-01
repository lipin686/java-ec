package com.example.demo.service;

import com.example.demo.dto.request.AddToCartRequest;
import com.example.demo.dto.request.UpdateCartItemRequest;
import com.example.demo.dto.response.frontend.CartResponse;

import java.util.List;

/**
 * 購物車服務接口
 */
public interface CartService {

    /**
     * 獲取用戶的購物車（通過用戶ID）
     * @param userId 用戶ID
     * @return 購物車響應
     */
    CartResponse getCart(Long userId);

    /**
     * 獲取用戶的購物車（通過email）
     * @param email 用戶email
     * @return 購物車響應
     */
    CartResponse getCartByEmail(String email);

    /**
     * 添加商品到購物車（通過用戶ID）
     * @param userId 用戶ID
     * @param request 添加請求
     * @return 購物車響應
     */
    CartResponse addToCart(Long userId, AddToCartRequest request);

    /**
     * 添加商品到購物車（通過email）
     * @param email 用戶email
     * @param request 添加請求
     * @return 購物車響應
     */
    CartResponse addToCartByEmail(String email, AddToCartRequest request);

    /**
     * 更新購物車項目數量（通過用戶ID）
     * @param userId 用戶ID
     * @param cartItemId 購物車項目ID
     * @param request 更新請求
     * @return 購物車響應
     */
    CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request);

    /**
     * 更新購物車項目數量（通過email）
     * @param email 用戶email
     * @param cartItemId 購物車項目ID
     * @param request 更新請求
     * @return 購物車響應
     */
    CartResponse updateCartItemByEmail(String email, Long cartItemId, UpdateCartItemRequest request);

    /**
     * 刪除購物車項目（通過用戶ID）
     * @param userId 用戶ID
     * @param cartItemId 購物車項目ID
     * @return 購物車響應
     */
    CartResponse removeCartItem(Long userId, Long cartItemId);

    /**
     * 刪除購物車項目（通過email）
     * @param email 用戶email
     * @param cartItemId 購物車項目ID
     * @return 購物車響應
     */
    CartResponse removeCartItemByEmail(String email, Long cartItemId);

    /**
     * 切換購物車項目的勾選狀態（通過用戶ID）
     * @param userId 用戶ID
     * @param cartItemId 購物車項目ID
     * @return 購物車響應
     */
    CartResponse toggleCartItemChecked(Long userId, Long cartItemId);

    /**
     * 切換購物車項目的勾選狀態（通過email）
     * @param email 用戶email
     * @param cartItemId 購物車項目ID
     * @return 購物車響應
     */
    CartResponse toggleCartItemCheckedByEmail(String email, Long cartItemId);

    /**
     * 全選/取消全選購物車項目（通過用戶ID）
     * @param userId 用戶ID
     * @param checked 是否勾選
     * @return 購物車響應
     */
    CartResponse toggleAllCartItems(Long userId, Boolean checked);

    /**
     * 全選/取消全選購物車項目（通過email）
     * @param email 用戶email
     * @param checked 是否勾選
     * @return 購物車響應
     */
    CartResponse toggleAllCartItemsByEmail(String email, Boolean checked);

    /**
     * 清空購物車（通過用戶ID）
     * @param userId 用戶ID
     */
    void clearCart(Long userId);

    /**
     * 清空購物車（通過email）
     * @param email 用戶email
     */
    void clearCartByEmail(String email);

    /**
     * 批量刪除購物車項目
     * @param userId 用戶ID
     * @param cartItemIds 購物車項目ID列表
     * @return 購物車響應
     */
    CartResponse batchRemoveCartItems(Long userId, List<Long> cartItemIds);

    /**
     * 刪除已勾選的購物車項目
     * @param userId 用戶ID
     * @return 購物車響應
     */
    CartResponse removeCheckedItems(Long userId);

    /**
     * 獲取已勾選的購物車項目（用於結帳）
     * @param userId 用戶ID
     * @return 購物車響應（只包含已勾選項目）
     */
    CartResponse getCheckedItems(Long userId);

    /**
     * 獲取購物車項目數量（輕量級接口）
     * @param userId 用戶ID
     * @return 購物車項目數量
     */
    Integer getCartItemCount(Long userId);

    /**
     * 驗證購物車項目（檢查庫存和商品狀態）
     * @param userId 用戶ID
     * @return 購物車響應（包含驗證信息）
     */
    CartResponse validateCart(Long userId);
}

