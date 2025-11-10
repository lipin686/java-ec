package com.example.demo.dto.request.backend;

import com.example.demo.enums.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UpdateProductRequest {

    @Size(max = 100, message = "商品名稱不能超過100個字符")
    private String name;

    @Size(max = 1000, message = "商品描述不能超過1000個字符")
    private String description;

    @DecimalMin(value = "0.01", message = "價格必須大於0")
    @Digits(integer = 17, fraction = 2, message = "價格格式不正確")
    private BigDecimal price;

    @Min(value = 0, message = "庫存數量不能為負數")
    private Integer stock;

    @Size(max = 500, message = "圖片URL不能超過500個字符")
    private String imageUrl;

    private ProductStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
