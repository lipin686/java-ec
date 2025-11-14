package com.example.demo.controller.frontend;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.frontend.ProductResponse;
import com.example.demo.service.frontend.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("frontendProductController")
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("")
    public ApiResponse<List<ProductResponse>> getAvailableProducts() {
        List<ProductResponse> products = productService.getAvailableProducts();
        return ApiResponse.success("獲取商品成功", products);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductDetail(@PathVariable Long id) {
        try {
            ProductResponse product = productService.getProductDetail(id);
            return ApiResponse.success("獲取商品成功", product);
        } catch (RuntimeException e) {
            return ApiResponse.error("商品不存在或未上架/已關閉");
        }
    }
}
