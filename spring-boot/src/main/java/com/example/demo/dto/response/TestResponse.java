package com.example.demo.dto.response;

import lombok.Data;
import lombok.Builder;

/**
 * 測試用響應 DTO
 */
@Data
@Builder
public class TestResponse {
    private Long id;
    private String name;
}
