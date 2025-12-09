package com.example.demo.repository;

import com.example.demo.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 訂單項目Repository
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 根據訂單ID查詢所有訂單項目
     */
    List<OrderItem> findByOrderId(Long orderId);

    /**
     * 根據產品ID查詢所有訂單項目
     */
    List<OrderItem> findByProductId(Long productId);
}

