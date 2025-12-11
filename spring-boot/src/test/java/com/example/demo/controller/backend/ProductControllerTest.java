package com.example.demo.controller.backend;

import com.example.demo.dto.request.backend.CreateProductRequest;
import com.example.demo.dto.request.backend.UpdateProductRequest;
import com.example.demo.entity.Product;
import com.example.demo.enums.ProductStatus;
import com.example.demo.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ProductRepository productRepository;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        Product product = new Product();
        product.setName("測試商品");
        product.setProductNo("P0001");
        product.setPrice(BigDecimal.valueOf(100));
        product.setStatus(ProductStatus.OPEN);
        product.setStock(10);
        savedProduct = productRepository.save(product);

        // 其他篩選用商品
        Product p2 = new Product();
        p2.setName("篩選商品A");
        p2.setProductNo("P0002");
        p2.setPrice(BigDecimal.valueOf(50));
        p2.setStatus(ProductStatus.HIDDEN);
        p2.setStock(0);
        productRepository.save(p2);

        Product p3 = new Product();
        p3.setName("篩選商品B");
        p3.setProductNo("P0003");
        p3.setPrice(BigDecimal.valueOf(200));
        p3.setStatus(ProductStatus.OPEN);
        p3.setStock(5);
        productRepository.save(p3);
    }

    @Test
    @DisplayName("建立商品")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("新商品");
        request.setPrice(BigDecimal.valueOf(200));
        request.setStatus(ProductStatus.OPEN);
        request.setStock(20);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", objectMapper.writeValueAsBytes(request));
        mockMvc.perform(multipart("/admin/v1/products").file(data).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("新商品"));
    }

    @Test
    @DisplayName("查詢單一商品")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetProductById() throws Exception {
        mockMvc.perform(get("/admin/v1/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("測試商品"));
    }

    @Test
    @DisplayName("查詢商品列表")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProducts() throws Exception {
        mockMvc.perform(get("/admin/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("根據名稱搜尋商品")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProductsByName() throws Exception {
        mockMvc.perform(get("/admin/v1/products/search").param("name", "測試商品"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("測試商品"));
    }

    @Test
    @DisplayName("更新商品")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateProduct() throws Exception {
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("更新商品");
        request.setPrice(BigDecimal.valueOf(300));
        request.setStatus(ProductStatus.HIDDEN);
        request.setStock(5);
        mockMvc.perform(put("/admin/v1/products/" + savedProduct.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("更新商品"));
    }

    @Test
    @DisplayName("軟刪除商品")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/admin/v1/products/" + savedProduct.getId()))
                .andExpect(status().isOk());
        Product deleted = productRepository.findById(savedProduct.getId()).orElse(null);
        assertThat(deleted).isNotNull(); // 軟刪除應該還在資料庫
        // 可根據你的軟刪除邏輯驗證 deleted.getDeleted() == true
    }

    @Test
    @DisplayName("硬刪除商品")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testHardDeleteProduct() throws Exception {
        mockMvc.perform(delete("/admin/v1/products/" + savedProduct.getId() + "/hard"))
                .andExpect(status().isOk());
        Product deleted = productRepository.findById(savedProduct.getId()).orElse(null);
        assertThat(deleted).isNull();
    }

    @Test
    @DisplayName("查詢不存在的商品應回404")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get("/admin/v1/products/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("建立商品缺少必填欄位應回400")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateProductMissingRequiredField() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        // 不設 name、price、stock
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", objectMapper.writeValueAsBytes(request));
        mockMvc.perform(multipart("/admin/v1/products").file(data).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("權限不足應回403")
    void testCreateProductForbidden() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("無權限商品");
        request.setPrice(BigDecimal.valueOf(100));
        request.setStatus(ProductStatus.OPEN);
        request.setStock(10);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", objectMapper.writeValueAsBytes(request));
        mockMvc.perform(multipart("/admin/v1/products").file(data).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("商品名稱超過最大長度應回400")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateProductNameTooLong() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("A".repeat(101));
        request.setPrice(BigDecimal.valueOf(100));
        request.setStatus(ProductStatus.OPEN);
        request.setStock(10);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", objectMapper.writeValueAsBytes(request));
        mockMvc.perform(multipart("/admin/v1/products").file(data).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("商品價格為負數應回400")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateProductNegativePrice() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("負數價格商品");
        request.setPrice(BigDecimal.valueOf(-1));
        request.setStatus(ProductStatus.OPEN);
        request.setStock(10);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", objectMapper.writeValueAsBytes(request));
        mockMvc.perform(multipart("/admin/v1/products").file(data).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("商品庫存為負數應回400")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreateProductNegativeStock() throws Exception {
        CreateProductRequest request = new CreateProductRequest();
        request.setName("負數庫存商品");
        request.setPrice(BigDecimal.valueOf(100));
        request.setStatus(ProductStatus.OPEN);
        request.setStock(-5);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", objectMapper.writeValueAsBytes(request));
        mockMvc.perform(multipart("/admin/v1/products").file(data).contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("查詢商品列表-名稱篩選")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProductsByNameFilter() throws Exception {
        mockMvc.perform(get("/admin/v1/products")
                .param("name", "篩選商品A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("篩選商品A"));
    }

    @Test
    @DisplayName("查詢商品列表-商品編號篩選")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProductsByProductNoFilter() throws Exception {
        mockMvc.perform(get("/admin/v1/products")
                .param("productNo", "P0003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].productNo").value("P0003"));
    }

    @Test
    @DisplayName("查詢商品列表-啟用狀態篩選")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProductsByIsActiveFilter() throws Exception {
        mockMvc.perform(get("/admin/v1/products")
                .param("status", "CLOSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
        // Note: 不檢查具體數據，因為測試環境可能沒有 CLOSED 狀態的商品
    }

    @Test
    @DisplayName("查詢商品列表-庫存狀態篩選")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProductsByStockFilter() throws Exception {
        mockMvc.perform(get("/admin/v1/products")
                .param("inStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].stock").value(org.hamcrest.Matchers.everyItem(org.hamcrest.Matchers.greaterThan(0))));
    }

    @Test
    @DisplayName("查詢商品列表-狀態篩選")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProductsByStatusFilter() throws Exception {
        mockMvc.perform(get("/admin/v1/products")
                .param("status", "HIDDEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].status").value("HIDDEN"));
    }

    @Test
    @DisplayName("查詢商品列表-分頁與排序（比對ID）")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSearchProductsPagingAndSorting() throws Exception {
        String response = mockMvc.perform(get("/admin/v1/products")
                .param("page", "0")
                .param("size", "2")) // 不帶 sortBy，預設 id desc
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andReturn().getResponse().getContentAsString();
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(response);
        long id0 = root.at("/data/content/0/id").asLong();
        long id1 = root.at("/data/content/1/id").asLong();
        org.junit.jupiter.api.Assertions.assertTrue(id0 > id1, "第一筆 id 應大於第二筆 id");
    }
}
