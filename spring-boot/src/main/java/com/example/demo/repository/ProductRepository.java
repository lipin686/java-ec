package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    // 查找未刪除商品（忽略大小寫）
    List<Product> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String name);

    // 根據商品編號查找未刪除商品
    Product findByProductNoAndDeletedAtIsNull(String productNo);

    // 查詢上架期間內且狀態為開啟且未刪除的商品
    List<Product> findByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqualAndDeletedAtIsNull(
            com.example.demo.enums.ProductStatus status, java.time.LocalDateTime now1, java.time.LocalDateTime now2);
}
