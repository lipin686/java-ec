package com.example.demo.service.frontend.impl;

import com.example.demo.dto.response.frontend.ProductResponse;
import com.example.demo.entity.Product;
import com.example.demo.enums.ProductStatus;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.frontend.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service("frontendProductServiceImpl")
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductResponse> getAvailableProducts() {
        LocalDateTime now = LocalDateTime.now();
        List<Product> products = productRepository.findByStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqualAndDeletedAtIsNull(
                ProductStatus.OPEN, now, now);
        return products.stream()
                .map(productMapper::toProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse getProductDetail(Long id) {
        LocalDateTime now = LocalDateTime.now();
        Product product = productRepository.findById(id)
                .filter(p -> p.getStatus() == ProductStatus.OPEN)
                .filter(p -> p.getStartAt() != null && p.getEndAt() != null &&
                        !now.isBefore(p.getStartAt()) && !now.isAfter(p.getEndAt()))
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new RuntimeException("商品不存在或未上架/已關閉"));
        return productMapper.toProductResponse(product);
    }
}
