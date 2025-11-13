package com.example.demo.service.frontend;

import com.example.demo.dto.response.frontend.ProductResponse;
import java.util.List;

public interface ProductService {
    List<ProductResponse> getAvailableProducts();
    ProductResponse getProductDetail(Long id);
}
