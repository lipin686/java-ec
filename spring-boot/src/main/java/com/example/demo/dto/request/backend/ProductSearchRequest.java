package com.example.demo.dto.request.backend;

import lombok.Data;

@Data
public class ProductSearchRequest {
    private String name;
    private String productNo;
    private Boolean inStock;
    private Boolean deleted;
    private String status;
    private String startAtFrom;
    private String startAtTo;
}
