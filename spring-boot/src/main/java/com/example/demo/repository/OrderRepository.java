package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 訂單Repository
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * 根據訂單編號查詢訂單
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * 根據用戶ID查詢所有訂單
     */
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 根據用戶ID和訂單狀態查詢訂單
     */
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status);

    /**
     * 根據用戶ID和訂單ID查詢訂單（預加載訂單項目）
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id AND o.userId = :userId")
    Optional<Order> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根據訂單編號和用戶ID查詢訂單（預加載訂單項目）
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.orderNumber = :orderNumber AND o.userId = :userId")
    Optional<Order> findByOrderNumberAndUserId(@Param("orderNumber") String orderNumber, @Param("userId") Long userId);

    /**
     * 檢查訂單編號是否存在
     */
    boolean existsByOrderNumber(String orderNumber);

    /**
     * 查詢用戶訂單數量
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 查詢用戶特定狀態的訂單數量
     */
    Long countByUserIdAndStatus(Long userId, OrderStatus status);
}

