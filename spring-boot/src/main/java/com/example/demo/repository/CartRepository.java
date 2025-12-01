package com.example.demo.repository;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 購物車資料存取介面
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * 根據會員ID查找購物車
     * @param userId 會員ID
     * @return 購物車
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * 檢查會員是否已有購物車
     * @param userId 會員ID
     * @return 是否存在
     */
    boolean existsByUserId(Long userId);
}


