package com.example.demo.repository;

import com.example.demo.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 購物車明細資料存取介面
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * 根據購物車ID查找所有項目
     * @param cartId 購物車ID
     * @return 購物車項目列表
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * 根據購物車ID和商品ID查找項目
     * @param cartId 購物車ID
     * @param productId 商品ID
     * @return 購物車項目
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * 刪除購物車中的所有項目
     * @param cartId 購物車ID
     */
    void deleteByCartId(Long cartId);

    /**
     * 查找購物車中已勾選的項目
     * @param cartId 購物車ID
     * @return 已勾選的購物車項目列表
     */
    List<CartItem> findByCartIdAndCheckedTrue(Long cartId);

    /**
     * 根據購物車ID和勾選狀態查找項目
     * @param cartId 購物車ID
     * @param checked 勾選狀態
     * @return 購物車項目列表
     */
    List<CartItem> findByCartIdAndChecked(Long cartId, Boolean checked);

    /**
     * 批量更新勾選狀態
     * @param cartId 購物車ID
     * @param checked 勾選狀態
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.checked = :checked WHERE ci.cartId = :cartId")
    void updateCheckedStatusByCartId(@Param("cartId") Long cartId, @Param("checked") Boolean checked);

    /**
     * 統計購物車項目數量
     * @param cartId 購物車ID
     * @return 項目數量
     */
    long countByCartId(Long cartId);
}

