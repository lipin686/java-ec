package com.example.demo.controller.frontend;

import com.example.demo.dto.request.AddToCartRequest;
import com.example.demo.dto.request.UpdateCartItemRequest;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.enums.ProductStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import com.example.demo.security.CustomUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 購物車控制器測試
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;
    private Cart testCart;
    private CartItem testCartItem;
    private CustomUserDetails customUserDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // 建立測試用戶
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setName("測試用戶");
        testUser.setRoles(Set.of(UserRole.USER));
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        // 建立測試商品1
        testProduct1 = new Product();
        testProduct1.setName("測試商品1");
        testProduct1.setProductNo("P0001");
        testProduct1.setPrice(BigDecimal.valueOf(100));
        testProduct1.setStatus(ProductStatus.OPEN);
        testProduct1.setStock(50);
        testProduct1.setDescription("測試商品描述1");
        testProduct1 = productRepository.save(testProduct1);

        // 建立測試商品2
        testProduct2 = new Product();
        testProduct2.setName("測試商品2");
        testProduct2.setProductNo("P0002");
        testProduct2.setPrice(BigDecimal.valueOf(200));
        testProduct2.setStatus(ProductStatus.OPEN);
        testProduct2.setStock(30);
        testProduct2.setDescription("測試商品描述2");
        testProduct2 = productRepository.save(testProduct2);

        // 建立測試購物車
        testCart = new Cart();
        testCart.setUserId(testUser.getId());
        testCart = cartRepository.save(testCart);

        // 建立測試購物車項目
        testCartItem = new CartItem();
        testCartItem.setCartId(testCart.getId());
        testCartItem.setProductId(testProduct1.getId());
        testCartItem.setQuantity(2);
        testCartItem.setChecked(true);
        testCartItem = cartItemRepository.save(testCartItem);

        // 創建 CustomUserDetails
        customUserDetails = new CustomUserDetails(
                testUser.getId(),
                testUser.getEmail(),
                testUser.getPassword(),
                testUser.getEnabled(),
                testUser.getAccountNonExpired(),
                testUser.getCredentialsNonExpired(),
                testUser.getAccountNonLocked(),
                testUser.getRoles().stream()
                        .map(role -> (GrantedAuthority) () -> "ROLE_" + role.name())
                        .toList()
        );

        // 創建 Authentication 對象
        authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                customUserDetails.getPassword(),
                customUserDetails.getAuthorities()
        );

        // 設置 SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("獲取當前用戶的購物車")
    void testGetCart() throws Exception {
        mockMvc.perform(get("/api/v1/cart")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("獲取購物車成功"))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].productId").value(testProduct1.getId()))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2));
    }

    @Test
    @DisplayName("添加商品到購物車 - 成功")

    void testAddToCart_Success() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(testProduct2.getId());
        request.setQuantity(3);

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("添加商品成功"))
                .andExpect(jsonPath("$.data.items").isArray());

        // 驗證資料庫
        List<CartItem> items = cartItemRepository.findByCartId(testCart.getId());
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("添加商品到購物車 - 商品不存在")

    void testAddToCart_ProductNotFound() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(99999L);
        request.setQuantity(1);

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品不存在"));
    }

    @Test
    @DisplayName("添加商品到購物車 - 庫存不足")

    void testAddToCart_InsufficientStock() throws Exception {
        AddToCartRequest request = new AddToCartRequest();
        request.setProductId(testProduct1.getId());
        request.setQuantity(100); // 超過庫存50

        mockMvc.perform(post("/api/v1/cart/items")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品庫存不足"));
    }

    @Test
    @DisplayName("更新購物車項目數量 - 成功")

    void testUpdateCartItem_Success() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        mockMvc.perform(put("/api/v1/cart/items/" + testCartItem.getId())
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("更新購物車項目成功"));

        // 驗證資料庫
        CartItem updated = cartItemRepository.findById(testCartItem.getId()).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("更新購物車項目數量 - 購物車項目不存在")

    void testUpdateCartItem_NotFound() throws Exception {
        UpdateCartItemRequest request = new UpdateCartItemRequest();
        request.setQuantity(5);

        mockMvc.perform(put("/api/v1/cart/items/99999")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("購物車項目不存在"));
    }

    @Test
    @DisplayName("刪除購物車項目 - 成功")

    void testRemoveCartItem_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/cart/items/" + testCartItem.getId())
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("刪除購物車項目成功"));

        // 驗證資料庫
        assertThat(cartItemRepository.findById(testCartItem.getId())).isEmpty();
    }

    @Test
    @DisplayName("切換購物車項目的勾選狀態")

    void testToggleCartItemChecked() throws Exception {
        boolean originalChecked = testCartItem.getChecked();

        mockMvc.perform(patch("/api/v1/cart/items/" + testCartItem.getId() + "/toggle")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("切換勾選狀態成功"));

        // 驗證資料庫
        CartItem updated = cartItemRepository.findById(testCartItem.getId()).orElseThrow();
        assertThat(updated.getChecked()).isEqualTo(!originalChecked);
    }

    @Test
    @DisplayName("全選購物車項目")

    void testToggleAllCartItems_CheckAll() throws Exception {
        mockMvc.perform(patch("/api/v1/cart/items/toggle-all")
                        .with(authentication(authentication))
                        .param("checked", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量切換勾選狀態成功"));
    }

    @Test
    @DisplayName("取消全選購物車項目")

    void testToggleAllCartItems_UncheckAll() throws Exception {
        mockMvc.perform(patch("/api/v1/cart/items/toggle-all")
                        .with(authentication(authentication))
                        .param("checked", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量切換勾選狀態成功"));
    }

    @Test
    @DisplayName("清空購物車")

    void testClearCart() throws Exception {
        mockMvc.perform(delete("/api/v1/cart")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("清空購物車成功"));

        // 驗證資料庫
        List<CartItem> items = cartItemRepository.findByCartId(testCart.getId());
        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("批量刪除購物車項目 - 成功")

    void testBatchRemoveCartItems_Success() throws Exception {
        // 建立第二個購物車項目
        CartItem item2 = new CartItem();
        item2.setCartId(testCart.getId());
        item2.setProductId(testProduct2.getId());
        item2.setQuantity(1);
        item2.setChecked(true);
        item2 = cartItemRepository.save(item2);

        List<Long> itemIds = Arrays.asList(testCartItem.getId(), item2.getId());

        mockMvc.perform(delete("/api/v1/cart/items/batch")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("批量刪除成功"));

        // 驗證資料庫
        assertThat(cartItemRepository.findById(testCartItem.getId())).isEmpty();
        assertThat(cartItemRepository.findById(item2.getId())).isEmpty();
    }

    @Test
    @DisplayName("批量刪除購物車項目 - 購物車無該商品")

    void testBatchRemoveCartItems_NotFound() throws Exception {
        List<Long> itemIds = Arrays.asList(testCartItem.getId(), 99999L);

        mockMvc.perform(delete("/api/v1/cart/items/batch")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemIds)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("購物車無該商品: [99999]"));

        // 驗證資料庫 - 確保沒有刪除任何項目（事務性）
        assertThat(cartItemRepository.findById(testCartItem.getId())).isPresent();
    }

    @Test
    @DisplayName("刪除已勾選的購物車項目")

    void testRemoveCheckedItems() throws Exception {
        // 建立一個未勾選的項目
        CartItem uncheckedItem = new CartItem();
        uncheckedItem.setCartId(testCart.getId());
        uncheckedItem.setProductId(testProduct2.getId());
        uncheckedItem.setQuantity(1);
        uncheckedItem.setChecked(false);
        uncheckedItem = cartItemRepository.save(uncheckedItem);

        mockMvc.perform(delete("/api/v1/cart/items/checked")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("刪除已勾選項目成功"));

        // 驗證資料庫 - 已勾選的被刪除，未勾選的保留
        assertThat(cartItemRepository.findById(testCartItem.getId())).isEmpty();
        assertThat(cartItemRepository.findById(uncheckedItem.getId())).isPresent();
    }

    @Test
    @DisplayName("獲取已勾選的購物車項目")

    void testGetCheckedItems() throws Exception {
        // 建立一個未勾選的項目
        CartItem uncheckedItem = new CartItem();
        uncheckedItem.setCartId(testCart.getId());
        uncheckedItem.setProductId(testProduct2.getId());
        uncheckedItem.setQuantity(1);
        uncheckedItem.setChecked(false);
        cartItemRepository.save(uncheckedItem);

        mockMvc.perform(get("/api/v1/cart/checked")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("獲取已勾選項目成功"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].checked").value(true));
    }

    @Test
    @DisplayName("獲取購物車項目數量")

    void testGetCartItemCount() throws Exception {
        mockMvc.perform(get("/api/v1/cart/count")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("獲取購物車數量成功"))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("驗證購物車 - 全部正常")

    void testValidateCart_AllValid() throws Exception {
        mockMvc.perform(post("/api/v1/cart/validate")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("購物車驗證完成"));
    }

    @Test
    @DisplayName("驗證購物車 - 庫存不足")

    void testValidateCart_InsufficientStock() throws Exception {
        // 修改商品庫存為不足
        testProduct1.setStock(1);
        productRepository.save(testProduct1);

        mockMvc.perform(post("/api/v1/cart/validate")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("庫存不足")));
    }

    @Test
    @DisplayName("未登入用戶無法訪問購物車")
    void testAccessCart_Unauthorized() throws Exception {
        // 清除 SecurityContext 以模擬未登入狀態
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/cart"))
                .andExpect(status().isForbidden()); // Spring Security 返回 403 而不是 401
    }
}

