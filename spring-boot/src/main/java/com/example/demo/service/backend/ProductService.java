package com.example.demo.service.backend;

import com.example.demo.dto.request.backend.CreateProductRequest;
import com.example.demo.dto.request.backend.UpdateProductRequest;
import com.example.demo.dto.response.backend.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    /**
     * 創建商品
     */
    ProductResponse createProduct(CreateProductRequest request, MultipartFile imageFile);

    /**
     * 根據ID獲取商品
     */
    ProductResponse getProductById(Long id);

    /**
     * 根據名稱搜索商品
     */
    List<ProductResponse> searchProductsByName(String name);

    /**
     * 更新商品
     */
    ProductResponse updateProduct(Long id, UpdateProductRequest request);

    /**
     * 刪除商品（軟刪除，設置為非啟用）
     */
    void deleteProduct(Long id);

    /**
     * 完全刪除商品
     */
    void hardDeleteProduct(Long id);

    /**
     * 商品複合篩選查詢（分頁）
     */
    Page<ProductResponse> searchProducts(
            String name,
            String productNo,
            Boolean inStock,
            Boolean deleted,
            String status,
            String startAtFrom,
            String startAtTo,
            Pageable pageable
    );
}
