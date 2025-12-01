package com.example.demo.dto.response.frontend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 購物車項目響應DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private String productImageUrl;
    private Integer productStock;
    private Integer quantity;
    private Boolean checked;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 商品狀態標記
    private Boolean productDeleted; // 商品是否已下架
    private Boolean stockInsufficient; // 庫存是否不足
    private String errorMessage; // 錯誤信息
}

