package com.example.demo.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加商品到購物車請求DTO
 */
@Data
public class AddToCartRequest {

    @NotNull(message = "商品ID不能為空")
    private Long productId;

    @NotNull(message = "數量不能為空")
    @Min(value = 1, message = "數量必須大於0")
    private Integer quantity;
}



