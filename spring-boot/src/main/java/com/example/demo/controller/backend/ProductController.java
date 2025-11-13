package com.example.demo.controller.backend;

import com.example.demo.dto.request.backend.CreateProductRequest;
import com.example.demo.dto.request.backend.UpdateProductRequest;
import com.example.demo.dto.request.backend.ProductSearchRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.backend.ProductResponse;
import com.example.demo.service.backend.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("backendProductController")
@RequestMapping("/admin/v1/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {

    private final ProductService productService;

    /**
     * 創建商品
     */
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestPart("data") @Valid CreateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) {
        ProductResponse product = productService.createProduct(request, imageFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("商品創建成功", product));
    }

    /**
     * 取得所有商品（分頁、多條件查詢）
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> searchProducts(
            @ModelAttribute ProductSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProductResponse> products = productService.searchProducts(
                request.getName(),
                request.getProductNo(),
                request.getInStock(),
                request.getDeleted(),
                request.getStatus(),
                request.getStartAtFrom(),
                request.getStartAtTo(),
                pageable);
        return ResponseEntity.ok(ApiResponse.success("取得商品列表成功", products));
    }

    /**
     * 根據ID獲取商品
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("獲取商品成功", product));
    }

    /**
     * 根據名稱搜索商品
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProducts(@RequestParam String name) {
        List<ProductResponse> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(ApiResponse.success("搜索商品成功", products));
    }

    /**
     * 更新商品
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("商品更新成功", product));
    }

    /**
     * 刪除商品（軟刪除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("商品刪除成功", null));
    }

    /**
     * 完全刪除商品
     */
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<ApiResponse<Void>> hardDeleteProduct(@PathVariable Long id) {
        productService.hardDeleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("商品永久刪除成功", null));
    }
}
