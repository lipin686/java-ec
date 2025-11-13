package com.example.demo.controller.frontend;

import com.example.demo.entity.Product;
import com.example.demo.enums.ProductStatus;
import com.example.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private Long availableProductId;
    private Long notYetAvailableProductId;
    private Long unavailableProductId;
    private Long hiddenProductId;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll(); // 確保每次測試前資料庫是乾淨的
        LocalDateTime now = LocalDateTime.now();
        // 上架且開啟
        Product p1 = new Product();
        p1.setName("前台商品A");
        p1.setProductNo("F001");
        p1.setPrice(BigDecimal.valueOf(100));
        p1.setStatus(ProductStatus.OPEN);
        p1.setStock(10);
        p1.setStartAt(now.minusDays(1));
        p1.setEndAt(now.plusDays(10));
        availableProductId = productRepository.save(p1).getId();

        // 未上架（startAt在未來）
        Product p2 = new Product();
        p2.setName("未上架商品");
        p2.setProductNo("F002");
        p2.setPrice(BigDecimal.valueOf(200));
        p2.setStatus(ProductStatus.OPEN);
        p2.setStock(5);
        p2.setStartAt(now.plusDays(1));
        p2.setEndAt(now.plusDays(10));
        notYetAvailableProductId = productRepository.save(p2).getId();

        // 已下架（endAt在過去）
        Product p3 = new Product();
        p3.setName("已下架商品");
        p3.setProductNo("F003");
        p3.setPrice(BigDecimal.valueOf(300));
        p3.setStatus(ProductStatus.OPEN);
        p3.setStock(8);
        p3.setStartAt(now.minusDays(10));
        p3.setEndAt(now.minusDays(1));
        unavailableProductId = productRepository.save(p3).getId();

        // 狀態關閉
        Product p4 = new Product();
        p4.setName("關閉商品");
        p4.setProductNo("F004");
        p4.setPrice(BigDecimal.valueOf(400));
        p4.setStatus(ProductStatus.HIDDEN);
        p4.setStock(8);
        p4.setStartAt(now.minusDays(1));
        p4.setEndAt(now.plusDays(10));
        hiddenProductId = productRepository.save(p4).getId();
    }

    @Test
    @DisplayName("前台商品列表-只回傳上架且開啟的商品")
    void testGetAvailableProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("前台商品A"));
    }

    @Test
    @DisplayName("前台商品詳細-查詢上架且開啟商品成功")
    void testGetProductDetail_success() throws Exception {
        mockMvc.perform(get("/api/v1/products/" + availableProductId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("前台商品A"));
    }

    @Test
    @DisplayName("前台商品詳細-查詢不存在商品id")
    void testGetProductDetail_notFound() throws Exception {
        Long notExistId = 99999L;
        mockMvc.perform(get("/api/v1/products/" + notExistId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品不存在或未上架/已關閉"));
    }

    @Test
    @DisplayName("前台商品詳細-查詢未上架/已下架/關閉商品")
    void testGetProductDetail_unavailable() throws Exception {
        // 未上架
        mockMvc.perform(get("/api/v1/products/" + notYetAvailableProductId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品不存在或未上架/已關閉"));
        // 已下架
        mockMvc.perform(get("/api/v1/products/" + unavailableProductId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品不存在或未上架/已關閉"));
        // 關閉
        mockMvc.perform(get("/api/v1/products/" + hiddenProductId).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品不存在或未上架/已關閉"));
    }
}
