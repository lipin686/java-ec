package com.example.demo.service.frontend.impl;

import com.example.demo.dto.request.frontend.CreateOrderRequest;
import com.example.demo.dto.response.frontend.OrderResponse;
import com.example.demo.entity.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.repository.*;
import com.example.demo.service.frontend.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 訂單服務實現
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Override
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        // 1. 獲取用戶購物車
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("購物車不存在"));

        // 2. 獲取已勾選的購物車項目
        List<CartItem> checkedItems = cartItemRepository.findByCartIdAndChecked(cart.getId(), true);

        if (checkedItems.isEmpty()) {
            throw new RuntimeException("請先選擇要結帳的商品");
        }

        // 3. 驗證商品庫存和狀態
        for (CartItem cartItem : checkedItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在：" + cartItem.getProductId()));

            if (product.getDeletedAt() != null) {
                throw new RuntimeException("商品已下架：" + product.getName());
            }

            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("商品庫存不足：" + product.getName());
            }
        }

        // 4. 創建訂單
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(userId)
                .status(OrderStatus.PENDING)
                .receiverName(request.getReceiverName())
                .receiverPhone(request.getReceiverPhone())
                .receiverAddress(request.getReceiverAddress())
                .remark(request.getRemark())
                .totalAmount(BigDecimal.ZERO)
                .build();

        // 5. 先保存訂單以獲取 ID
        Order savedOrder = orderRepository.save(order);

        // 6. 創建訂單項目並扣減庫存
        for (CartItem cartItem : checkedItems) {
            Product product = productRepository.findById(cartItem.getProductId()).get();

            // 創建訂單項目（保存商品快照）
            OrderItem orderItem = OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productImage(product.getImageUrl())
                    .price(product.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();

            // 計算小計
            orderItem.calculateSubtotal();
            savedOrder.addOrderItem(orderItem);

            // 扣減庫存
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // 7. 計算訂單總金額
        savedOrder.calculateTotalAmount();

        // 8. 更新訂單總金額
        savedOrder = orderRepository.save(savedOrder);

        // 9. 刪除已結帳的購物車項目
        cartItemRepository.deleteAll(checkedItems);

        log.info("用戶 {} 創建訂單成功，訂單編號：{}", userId, savedOrder.getOrderNumber());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(Long userId, String orderNumber) {
        Order order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId, OrderStatus status) {
        List<Order> orders;

        if (status != null) {
            orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
        } else {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        }

        return orderMapper.toOrderResponseList(orders);
    }

    @Override
    public OrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new RuntimeException("訂單不存在"));

        // 只有待處理和已確認的訂單可以取消
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("訂單狀態不允許取消");
        }

        // 恢復庫存
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = productRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            product.setStock(product.getStock() + orderItem.getQuantity());
            productRepository.save(product);
        }

        // 更新訂單狀態
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        log.info("用戶 {} 取消訂單：{}", userId, order.getOrderNumber());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserOrderCount(Long userId, OrderStatus status) {
        if (status != null) {
            return orderRepository.countByUserIdAndStatus(userId, status);
        } else {
            return orderRepository.countByUserId(userId);
        }
    }

    /**
     * 生成唯一訂單編號
     * 格式：ORD + 年月日 + 時分秒 + 4位隨機數
     * 例如：ORD20231201143025XXXX
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        String orderNumber = "ORD" + timestamp + random;

        // 確保訂單編號唯一
        while (orderRepository.existsByOrderNumber(orderNumber)) {
            random = ThreadLocalRandom.current().nextInt(1000, 10000);
            orderNumber = "ORD" + timestamp + random;
        }

        return orderNumber;
    }
}

