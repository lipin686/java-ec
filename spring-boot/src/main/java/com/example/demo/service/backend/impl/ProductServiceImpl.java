package com.example.demo.service.backend.impl;

import com.example.demo.dto.request.backend.CreateProductRequest;
import com.example.demo.dto.request.backend.UpdateProductRequest;
import com.example.demo.dto.response.backend.ProductResponse;
import com.example.demo.entity.Product;
import com.example.demo.enums.ProductStatus;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.backend.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("backendProductServiceImpl")
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    private String generateRandomProductNo() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
    }

    @Override
    public ProductResponse createProduct(CreateProductRequest request, MultipartFile imageFile) {
        String productNo = generateRandomProductNo(); // 一律系統產生
        String imageUrl = handleProductImageUpload(imageFile, productNo);

        Product product = Product.builder()
                .productNo(productNo)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(imageUrl)
                .status(request.getStatus())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .build();
        Product savedProduct = productRepository.save(product);
        return convertToResponse(savedProduct);
    }

    private String handleProductImageUpload(MultipartFile imageFile, String productNo) {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }
        // 將圖片存到 public/images/products/ 目錄
        String uploadDir = "public/images/products/";
        String ext = extractFileExtension(imageFile.getOriginalFilename());
        String fileName = productNo + "_" + System.currentTimeMillis() + ext;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(fileName);
            imageFile.transferTo(filePath);
            return "/images/products/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("圖片上傳失敗", e);
        }
    }

    private String extractFileExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.'));
        }
        return "";
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("商品不存在，ID: " + id));
        return convertToResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndDeletedAtIsNull(name);
        return products.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("商品不存在，ID: " + id));
        productMapper.updateProductFromDto(request, product); // MapStruct自動只更新非null欄位
        Product updatedProduct = productRepository.save(product);
        return convertToResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("商品不存在，ID: " + id));
        product.setDeletedAt(java.time.LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    public void hardDeleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new UserNotFoundException("商品不存在，ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, String productNo, Boolean inStock, Boolean deleted, String status, String startAtFrom, String startAtTo, Pageable pageable) {
        Specification<Product> spec = buildProductSpecification(name, productNo, inStock, deleted, status, startAtFrom, startAtTo);
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(this::convertToResponse);
    }

    private Specification<Product> buildProductSpecification(String name, String productNo, Boolean inStock, Boolean deleted, String status, String startAtFrom, String startAtTo) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();
        // 名稱模糊查詢
        if (name != null && !name.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));
        }
        // 商品編號
        if (productNo != null && !productNo.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("productNo"), productNo));
        }
        // 是否有庫存
        if (inStock != null) {
            if (inStock) {
                spec = spec.and((root, query, cb) -> cb.greaterThan(root.get("stock"), 0));
            } else {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("stock"), 0));
            }
        }
        // 是否刪除
        if (deleted != null) {
            if (deleted) {
                spec = spec.and((root, query, cb) -> cb.isNotNull(root.get("deletedAt")));
            } else {
                spec = spec.and((root, query, cb) -> cb.isNull(root.get("deletedAt")));
            }
        } else {
            // 預設查未刪除
            spec = spec.and((root, query, cb) -> cb.isNull(root.get("deletedAt")));
        }
        // 狀態
        if (status != null && !status.isEmpty()) {
            ProductStatus productStatus = ProductStatus.parseOrNull(status);
            if (productStatus != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), productStatus));
            }
        }
        // startAt 範圍
        if (startAtFrom != null && !startAtFrom.isEmpty()) {
            java.time.LocalDateTime from = parseLocalDateTime(startAtFrom);
            if (from != null) {
                spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startAt"), from));
            }
        }
        if (startAtTo != null && !startAtTo.isEmpty()) {
            java.time.LocalDateTime to = parseLocalDateTime(startAtTo);
            if (to != null) {
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startAt"), to));
            }
        }
        return spec;
    }


    private java.time.LocalDateTime parseLocalDateTime(String dateTimeStr) {
        try {
            return java.time.LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            return null;
        }
    }

    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productNo(product.getProductNo())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .status(product.getStatus())
                .startAt(product.getStartAt())
                .endAt(product.getEndAt())
                .build();
    }
}
