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
}
