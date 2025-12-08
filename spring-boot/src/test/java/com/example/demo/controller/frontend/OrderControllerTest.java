package com.example.demo.controller.frontend;

import com.example.demo.dto.request.frontend.CreateOrderRequest;
import com.example.demo.entity.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.ProductStatus;
import com.example.demo.enums.UserRole;
import com.example.demo.repository.*;
import com.example.demo.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 訂單控制器測試
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

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
    private CartItem testCartItem1;
    private CartItem testCartItem2;
    private Order testOrder;
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
        testProduct1.setImageUrl("/images/product1.jpg");
        testProduct1 = productRepository.save(testProduct1);

        // 建立測試商品2
        testProduct2 = new Product();
        testProduct2.setName("測試商品2");
        testProduct2.setProductNo("P0002");
        testProduct2.setPrice(BigDecimal.valueOf(200));
        testProduct2.setStatus(ProductStatus.OPEN);
        testProduct2.setStock(30);
        testProduct2.setDescription("測試商品描述2");
        testProduct2.setImageUrl("/images/product2.jpg");
        testProduct2 = productRepository.save(testProduct2);

        // 建立測試購物車
        testCart = new Cart();
        testCart.setUserId(testUser.getId());
        testCart = cartRepository.save(testCart);

        // 建立測試購物車項目1（已勾選）
        testCartItem1 = new CartItem();
        testCartItem1.setCartId(testCart.getId());
        testCartItem1.setProductId(testProduct1.getId());
        testCartItem1.setQuantity(2);
        testCartItem1.setChecked(true);
        testCartItem1 = cartItemRepository.save(testCartItem1);

        // 建立測試購物車項目2（已勾選）
        testCartItem2 = new CartItem();
        testCartItem2.setCartId(testCart.getId());
        testCartItem2.setProductId(testProduct2.getId());
        testCartItem2.setQuantity(1);
        testCartItem2.setChecked(true);
        testCartItem2 = cartItemRepository.save(testCartItem2);

        // 建立測試訂單（模擬真實場景：創建訂單時扣除庫存）
        testOrder = new Order();
        testOrder.setOrderNumber("ORD202512021000000001");
        testOrder.setUserId(testUser.getId());
        testOrder.setStatus(OrderStatus.PENDING);
        testOrder.setTotalAmount(BigDecimal.valueOf(400));
        testOrder.setReceiverName("張三");
        testOrder.setReceiverPhone("0912345678");
        testOrder.setReceiverAddress("台北市信義區信義路五段7號");
        testOrder.setRemark("請在下午送達");

        // 先保存訂單以獲取 ID
        testOrder = orderRepository.save(testOrder);

        // 建立測試訂單項目
        OrderItem orderItem1 = OrderItem.builder()
                .orderId(testOrder.getId())
                .productId(testProduct1.getId())
                .productName(testProduct1.getName())
                .productImage(testProduct1.getImageUrl())
                .price(testProduct1.getPrice())
                .quantity(2)
                .subtotal(BigDecimal.valueOf(200))
                .build();

        OrderItem orderItem2 = OrderItem.builder()
                .orderId(testOrder.getId())
                .productId(testProduct2.getId())
                .productName(testProduct2.getName())
                .productImage(testProduct2.getImageUrl())
                .price(testProduct2.getPrice())
                .quantity(1)
                .subtotal(BigDecimal.valueOf(200))
                .build();

        // 添加訂單項目到訂單（設置雙向關聯）
        testOrder.addOrderItem(orderItem1);
        testOrder.addOrderItem(orderItem2);

        // 再次保存訂單（級聯保存訂單項目）
        testOrder = orderRepository.save(testOrder);

        // 扣除商品1的庫存（50 - 2 = 48）
        testProduct1.setStock(testProduct1.getStock() - 2);
        productRepository.save(testProduct1);

        // 扣除商品2的庫存（30 - 1 = 29）
        testProduct2.setStock(testProduct2.getStock() - 1);
        productRepository.save(testProduct2);

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
    @DisplayName("創建訂單 - 成功")
    void testCreateOrder_Success() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");
        request.setRemark("請在早上送達");

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("訂單創建成功"))
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.receiverName").value("李四"))
                .andExpect(jsonPath("$.data.receiverPhone").value("0987654321"))
                .andExpect(jsonPath("$.data.receiverAddress").value("新北市板橋區中山路一段1號"))
                .andExpect(jsonPath("$.data.remark").value("請在早上送達"))
                .andExpect(jsonPath("$.data.orderItems").isArray())
                .andExpect(jsonPath("$.data.orderNumber").exists());

        // 驗證購物車項目已被清除
        List<CartItem> remainingItems = cartItemRepository.findByCartIdAndChecked(testCart.getId(), true);
        assertThat(remainingItems).isEmpty();

        // 驗證訂單已創建
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertThat(orders).hasSizeGreaterThanOrEqualTo(2); // 包含 setUp 中的測試訂單
    }

    @Test
    @DisplayName("創建訂單 - 購物車為空")
    void testCreateOrder_EmptyCart() throws Exception {
        // 清空購物車
        cartItemRepository.deleteAll(List.of(testCartItem1, testCartItem2));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("請先選擇要結帳的商品"));
    }

    @Test
    @DisplayName("創建訂單 - 商品庫存不足")
    void testCreateOrder_InsufficientStock() throws Exception {
        // 設置商品庫存為0
        testProduct1.setStock(0);
        productRepository.save(testProduct1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品庫存不足：測試商品1"));
    }

    @Test
    @DisplayName("創建訂單 - 驗證欄位為空")
    void testCreateOrder_ValidationError() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        // 不設置必填欄位

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("查詢所有訂單")
    void testGetUserOrders_All() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("獲取訂單列表成功"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(testOrder.getId()))
                .andExpect(jsonPath("$.data[0].orderNumber").value(testOrder.getOrderNumber()))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("按狀態查詢訂單 - PENDING")
    void testGetUserOrders_ByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .param("status", "PENDING")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));
    }

    @Test
    @DisplayName("按狀態查詢訂單 - COMPLETED（無結果）")
    void testGetUserOrders_ByStatusNoResult() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .param("status", "COMPLETED")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("根據訂單ID查詢訂單詳情 - 成功")
    void testGetOrderById_Success() throws Exception {
        mockMvc.perform(get("/api/v1/orders/" + testOrder.getId())
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("獲取訂單詳情成功"))
                .andExpect(jsonPath("$.data.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.orderNumber").value(testOrder.getOrderNumber()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.receiverName").value("張三"))
                .andExpect(jsonPath("$.data.orderItems").isArray());
    }

    @Test
    @DisplayName("根據訂單ID查詢訂單詳情 - 訂單不存在")
    void testGetOrderById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/99999")
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單不存在"));
    }

    @Test
    @DisplayName("根據訂單編號查詢訂單詳情 - 成功")
    void testGetOrderByOrderNumber_Success() throws Exception {
        mockMvc.perform(get("/api/v1/orders/number/" + testOrder.getOrderNumber())
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("獲取訂單詳情成功"))
                .andExpect(jsonPath("$.data.orderNumber").value(testOrder.getOrderNumber()))
                .andExpect(jsonPath("$.data.id").value(testOrder.getId()));
    }

    @Test
    @DisplayName("根據訂單編號查詢訂單詳情 - 訂單不存在")
    void testGetOrderByOrderNumber_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/orders/number/INVALID_ORDER_NUMBER")
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單不存在"));
    }

    @Test
    @DisplayName("取消訂單 - 成功")
    void testCancelOrder_Success() throws Exception {
        // 先刷新數據確保訂單項目正確保存
        orderItemRepository.flush();

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("訂單取消成功"))
                .andExpect(jsonPath("$.data.id").value(testOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // 驗證訂單狀態已更新
        Order cancelledOrder = orderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(cancelledOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("取消訂單 - 訂單不存在")
    void testCancelOrder_NotFound() throws Exception {
        mockMvc.perform(patch("/api/v1/orders/99999/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單不存在"));
    }

    @Test
    @DisplayName("取消訂單 - 訂單狀態不允許取消")
    void testCancelOrder_InvalidStatus() throws Exception {
        // 修改訂單狀態為已出貨
        testOrder.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(testOrder);

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單狀態不允許取消"));
    }

    @Test
    @DisplayName("查詢訂單總數")
    void testGetUserOrderCount_All() throws Exception {
        mockMvc.perform(get("/api/v1/orders/count")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("獲取訂單數量成功"))
                .andExpect(jsonPath("$.data").value(1)); // setUp 中創建了1個訂單
    }

    @Test
    @DisplayName("按狀態查詢訂單數量 - PENDING")
    void testGetUserOrderCount_ByStatus() throws Exception {
        mockMvc.perform(get("/api/v1/orders/count")
                        .param("status", "PENDING")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    @DisplayName("按狀態查詢訂單數量 - COMPLETED（無結果）")
    void testGetUserOrderCount_ByStatusNoResult() throws Exception {
        mockMvc.perform(get("/api/v1/orders/count")
                        .param("status", "COMPLETED")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(0));
    }

    @Test
    @DisplayName("創建多個訂單並驗證訂單編號唯一性")
    void testMultipleOrders_UniqueOrderNumber() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("王五");
        request.setReceiverPhone("0911111111");
        request.setReceiverAddress("台中市西屯區台灣大道三段99號");

        // 創建第一個訂單
        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 重新添加購物車項目（先刪除可能存在的舊項目）
        cartItemRepository.deleteByCartId(testCart.getId());

        CartItem newCartItem = new CartItem();
        newCartItem.setCartId(testCart.getId());
        newCartItem.setProductId(testProduct1.getId());
        newCartItem.setQuantity(1);
        newCartItem.setChecked(true);
        cartItemRepository.save(newCartItem);

        // 創建第二個訂單
        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // 驗證訂單編號唯一
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());
        assertThat(orders).hasSizeGreaterThanOrEqualTo(3);

        Set<String> orderNumbers = orders.stream()
                .map(Order::getOrderNumber)
                .collect(java.util.stream.Collectors.toSet());
        assertThat(orderNumbers).hasSize(orders.size()); // 所有訂單編號都是唯一的
    }

    @Test
    @DisplayName("查詢訂單 - 未授權")
    void testGetOrders_Unauthorized() throws Exception {
        // 清除 SecurityContext
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isForbidden()); // Spring Security 返回 403 Forbidden
    }

    @Test
    @DisplayName("創建訂單 - 商品已下架")
    void testCreateOrder_ProductDeleted() throws Exception {
        // 將商品標記為已刪除
        testProduct1.setDeletedAt(java.time.LocalDateTime.now());
        productRepository.save(testProduct1);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("商品已下架：測試商品1"));
    }

    @Test
    @DisplayName("創建訂單 - 部分購物車項目未勾選")
    void testCreateOrder_OnlyCheckedItems() throws Exception {
        // 將第二個購物車項目設為未勾選
        testCartItem2.setChecked(false);
        cartItemRepository.save(testCartItem2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.orderItems").isArray())
                .andExpect(jsonPath("$.data.orderItems.length()").value(1)); // 只有一個商品

        // 驗證未勾選的購物車項目仍然存在
        CartItem remainingItem = cartItemRepository.findById(testCartItem2.getId()).orElse(null);
        assertThat(remainingItem).isNotNull();
        assertThat(remainingItem.getChecked()).isFalse();
    }

    @Test
    @DisplayName("創建訂單 - 驗證收件人姓名過長")
    void testCreateOrder_ReceiverNameTooLong() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("A".repeat(101)); // 超過100個字符
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("創建訂單 - 驗證收件地址過長")
    void testCreateOrder_AddressTooLong() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("A".repeat(501)); // 超過500個字符

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("創建訂單 - 驗證備註過長")
    void testCreateOrder_RemarkTooLong() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");
        request.setRemark("A".repeat(1001)); // 超過1000個字符

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("取消訂單 - CONFIRMED 狀態允許取消")
    void testCancelOrder_ConfirmedStatus() throws Exception {
        // 修改訂單狀態為已確認
        testOrder.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(testOrder);

        // 刷新訂單項目數據
        orderItemRepository.flush();

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("訂單取消成功"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"));

        // 驗證庫存已恢復
        Product product1 = productRepository.findById(testProduct1.getId()).orElseThrow();
        assertThat(product1.getStock()).isEqualTo(50); // 48 + 2 = 50（恢復到初始庫存）
    }

    @Test
    @DisplayName("取消訂單 - PROCESSING 狀態不允許取消")
    void testCancelOrder_ProcessingStatus() throws Exception {
        // 修改訂單狀態為處理中
        testOrder.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(testOrder);

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單狀態不允許取消"));
    }

    @Test
    @DisplayName("取消訂單 - DELIVERED 狀態不允許取消")
    void testCancelOrder_DeliveredStatus() throws Exception {
        // 修改訂單狀態為已送達
        testOrder.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(testOrder);

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單狀態不允許取消"));
    }

    @Test
    @DisplayName("取消訂單 - COMPLETED 狀態不允許取消")
    void testCancelOrder_CompletedStatus() throws Exception {
        // 修改訂單狀態為已完成
        testOrder.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(testOrder);

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單狀態不允許取消"));
    }

    @Test
    @DisplayName("取消訂單 - 驗證庫存正確恢復")
    void testCancelOrder_StockRestored() throws Exception {
        // 刷新產品數據以獲取最新的庫存值
        productRepository.flush();
        Product productBeforeCancel = productRepository.findById(testProduct1.getId()).orElseThrow();
        int stockBeforeCancel = productBeforeCancel.getStock(); // 應該是 48
        int orderQuantity = 2; // testOrder 中 testProduct1 的數量

        // 刷新訂單項目數據
        orderItemRepository.flush();

        mockMvc.perform(patch("/api/v1/orders/" + testOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isOk());

        // 驗證庫存已正確恢復
        Product product = productRepository.findById(testProduct1.getId()).orElseThrow();
        assertThat(product.getStock()).isEqualTo(stockBeforeCancel + orderQuantity); // 48 + 2 = 50
    }

    @Test
    @DisplayName("查詢訂單 - 按不同狀態查詢")
    void testGetUserOrders_ByDifferentStatuses() throws Exception {
        // 創建不同狀態的訂單
        Order confirmedOrder = new Order();
        confirmedOrder.setOrderNumber("ORD202512021000000002");
        confirmedOrder.setUserId(testUser.getId());
        confirmedOrder.setStatus(OrderStatus.CONFIRMED);
        confirmedOrder.setTotalAmount(BigDecimal.valueOf(300));
        confirmedOrder.setReceiverName("測試用戶2");
        confirmedOrder.setReceiverPhone("0912345678");
        confirmedOrder.setReceiverAddress("台北市");
        orderRepository.save(confirmedOrder);

        // 測試查詢 PENDING 狀態
        mockMvc.perform(get("/api/v1/orders")
                        .param("status", "PENDING")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.status == 'PENDING')]").exists());

        // 測試查詢 CONFIRMED 狀態
        mockMvc.perform(get("/api/v1/orders")
                        .param("status", "CONFIRMED")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.status == 'CONFIRMED')]").exists());
    }

    @Test
    @DisplayName("查詢訂單 - 驗證用戶只能查看自己的訂單")
    void testGetUserOrders_OnlyOwnOrders() throws Exception {
        // 創建另一個用戶和訂單
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password"));
        anotherUser.setName("另一個用戶");
        anotherUser.setRoles(Set.of(UserRole.USER));
        anotherUser.setEnabled(true);
        anotherUser = userRepository.save(anotherUser);

        Order anotherOrder = new Order();
        anotherOrder.setOrderNumber("ORD202512021000000003");
        anotherOrder.setUserId(anotherUser.getId());
        anotherOrder.setStatus(OrderStatus.PENDING);
        anotherOrder.setTotalAmount(BigDecimal.valueOf(500));
        anotherOrder.setReceiverName("另一個用戶");
        anotherOrder.setReceiverPhone("0912345678");
        anotherOrder.setReceiverAddress("高雄市");
        orderRepository.save(anotherOrder);

        // 當前用戶應該只能看到自己的訂單
        mockMvc.perform(get("/api/v1/orders")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.userId == " + anotherUser.getId() + ")]").doesNotExist());
    }

    @Test
    @DisplayName("根據訂單ID查詢 - 查詢其他用戶的訂單應該失敗")
    void testGetOrderById_OtherUserOrder() throws Exception {
        // 創建另一個用戶和訂單
        User anotherUser = new User();
        anotherUser.setEmail("another2@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password"));
        anotherUser.setName("另一個用戶2");
        anotherUser.setRoles(Set.of(UserRole.USER));
        anotherUser.setEnabled(true);
        anotherUser = userRepository.save(anotherUser);

        Order anotherOrder = new Order();
        anotherOrder.setOrderNumber("ORD202512021000000004");
        anotherOrder.setUserId(anotherUser.getId());
        anotherOrder.setStatus(OrderStatus.PENDING);
        anotherOrder.setTotalAmount(BigDecimal.valueOf(500));
        anotherOrder.setReceiverName("另一個用戶2");
        anotherOrder.setReceiverPhone("0912345678");
        anotherOrder.setReceiverAddress("高雄市");
        anotherOrder = orderRepository.save(anotherOrder);

        // 當前用戶嘗試查詢其他用戶的訂單
        mockMvc.perform(get("/api/v1/orders/" + anotherOrder.getId())
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單不存在"));
    }

    @Test
    @DisplayName("取消訂單 - 取消其他用戶的訂單應該失敗")
    void testCancelOrder_OtherUserOrder() throws Exception {
        // 創建另一個用戶和訂單
        User anotherUser = new User();
        anotherUser.setEmail("another3@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password"));
        anotherUser.setName("另一個用戶3");
        anotherUser.setRoles(Set.of(UserRole.USER));
        anotherUser.setEnabled(true);
        anotherUser = userRepository.save(anotherUser);

        Order anotherOrder = new Order();
        anotherOrder.setOrderNumber("ORD202512021000000005");
        anotherOrder.setUserId(anotherUser.getId());
        anotherOrder.setStatus(OrderStatus.PENDING);
        anotherOrder.setTotalAmount(BigDecimal.valueOf(500));
        anotherOrder.setReceiverName("另一個用戶3");
        anotherOrder.setReceiverPhone("0912345678");
        anotherOrder.setReceiverAddress("高雄市");
        anotherOrder = orderRepository.save(anotherOrder);

        // 當前用戶嘗試取消其他用戶的訂單
        mockMvc.perform(patch("/api/v1/orders/" + anotherOrder.getId() + "/cancel")
                        .with(authentication(authentication)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("訂單不存在"));
    }

    @Test
    @DisplayName("創建訂單 - 驗證訂單總金額計算正確")
    void testCreateOrder_TotalAmountCalculation() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");

        mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalAmount").value(400.00)); // 100*2 + 200*1 = 400
    }

    @Test
    @DisplayName("創建訂單 - 驗證訂單項目快照保存")
    void testCreateOrder_OrderItemSnapshot() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setReceiverName("李四");
        request.setReceiverPhone("0987654321");
        request.setReceiverAddress("新北市板橋區中山路一段1號");

        String response = mockMvc.perform(post("/api/v1/orders")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderItems[0].productName").value("測試商品1"))
                .andExpect(jsonPath("$.data.orderItems[0].price").value(100.00))
                .andExpect(jsonPath("$.data.orderItems[0].productImage").value("/images/product1.jpg"))
                .andReturn().getResponse().getContentAsString();

        // 修改商品資訊
        testProduct1.setName("修改後的商品名稱");
        testProduct1.setPrice(BigDecimal.valueOf(999));
        productRepository.save(testProduct1);

        // 驗證訂單項目保存的是快照，不受商品修改影響
        Long orderId = objectMapper.readTree(response).get("data").get("id").asLong();
        mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.orderItems[0].productName").value("測試商品1"))
                .andExpect(jsonPath("$.data.orderItems[0].price").value(100.00));
    }
}