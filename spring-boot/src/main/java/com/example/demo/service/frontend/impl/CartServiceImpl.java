package com.example.demo.service.frontend.impl;

import com.example.demo.dto.request.AddToCartRequest;
import com.example.demo.dto.request.UpdateCartItemRequest;
import com.example.demo.dto.response.frontend.CartItemResponse;
import com.example.demo.dto.response.frontend.CartResponse;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.exception.CartItemNotFoundException;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.frontend.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 購物車服務實現
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        // 驗證商品是否存在且未刪除
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getDeletedAt() != null) {
            throw new RuntimeException("商品已下架");
        }

        // 檢查庫存
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("商品庫存不足");
        }

        // 獲取或創建購物車
        Cart cart = getOrCreateCart(userId);

        // 檢查購物車中是否已有該商品
        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), request.getProductId())
                .orElse(null);

        if (existingItem != null) {
            // 如果已存在，更新數量
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new RuntimeException("商品庫存不足");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // 創建新的購物車項目
            CartItem cartItem = CartItem.builder()
                    .cartId(cart.getId())
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .checked(true)
                    .build();
            cartItemRepository.save(cartItem);
        }

        log.info("用戶 {} 添加商品 {} 到購物車，數量：{}", userId, request.getProductId(), request.getQuantity());
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(Long userId, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("購物車項目不存在"));

        // 驗證購物車項目屬於該用戶
        if (!cartItem.getCartId().equals(cart.getId())) {
            throw new RuntimeException("無權操作此購物車項目");
        }

        // 檢查商品庫存
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("商品庫存不足");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        log.info("用戶 {} 更新購物車項目 {} 數量為：{}", userId, cartItemId, request.getQuantity());
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse removeCartItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("購物車項目不存在"));

        // 驗證購物車項目屬於該用戶
        if (!cartItem.getCartId().equals(cart.getId())) {
            throw new RuntimeException("無權操作此購物車項目");
        }

        cartItemRepository.delete(cartItem);
        log.info("用戶 {} 刪除購物車項目 {}", userId, cartItemId);

        return buildCartResponse(cart);
    }

    @Override
    public CartResponse toggleCartItemChecked(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("購物車項目不存在"));

        // 驗證購物車項目屬於該用戶
        if (!cartItem.getCartId().equals(cart.getId())) {
            throw new RuntimeException("無權操作此購物車項目");
        }

        cartItem.setChecked(!cartItem.getChecked());
        cartItemRepository.save(cartItem);

        log.info("用戶 {} 切換購物車項目 {} 勾選狀態為：{}", userId, cartItemId, cartItem.getChecked());
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse toggleAllCartItems(Long userId, Boolean checked) {
        Cart cart = getOrCreateCart(userId);
        cartItemRepository.updateCheckedStatusByCartId(cart.getId(), checked);

        log.info("用戶 {} 全選/取消全選購物車項目，狀態：{}", userId, checked);
        return buildCartResponse(cart);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            log.info("用戶 {} 清空購物車", userId);
        }
    }

    // ============= Email-based methods =============

    @Override
    @Transactional
    public CartResponse getCartByEmail(String email) {
        Long userId = getUserIdByEmail(email);
        return getCart(userId);
    }

    @Override
    public CartResponse addToCartByEmail(String email, AddToCartRequest request) {
        Long userId = getUserIdByEmail(email);
        return addToCart(userId, request);
    }

    @Override
    public CartResponse updateCartItemByEmail(String email, Long cartItemId, UpdateCartItemRequest request) {
        Long userId = getUserIdByEmail(email);
        return updateCartItem(userId, cartItemId, request);
    }

    @Override
    public CartResponse removeCartItemByEmail(String email, Long cartItemId) {
        Long userId = getUserIdByEmail(email);
        return removeCartItem(userId, cartItemId);
    }

    @Override
    public CartResponse toggleCartItemCheckedByEmail(String email, Long cartItemId) {
        Long userId = getUserIdByEmail(email);
        return toggleCartItemChecked(userId, cartItemId);
    }

    @Override
    public CartResponse toggleAllCartItemsByEmail(String email, Boolean checked) {
        Long userId = getUserIdByEmail(email);
        return toggleAllCartItems(userId, checked);
    }

    @Override
    public void clearCartByEmail(String email) {
        Long userId = getUserIdByEmail(email);
        clearCart(userId);
    }

    @Override
    public CartResponse batchRemoveCartItems(Long userId, List<Long> cartItemIds) {
        Cart cart = getOrCreateCart(userId);

        List<CartItem> itemsToDelete = cartItemRepository.findAllById(cartItemIds);
        // 用 Set 提升效率
        Set<Long> foundIds = itemsToDelete.stream().map(CartItem::getId).collect(Collectors.toSet());
        // 一次找出所有不存在的 id
        List<Long> notFoundIds = cartItemIds.stream()
            .filter(id -> !foundIds.contains(id))
            .toList();
        if (!notFoundIds.isEmpty()) {
            throw new CartItemNotFoundException(notFoundIds);
        }
        // 驗證所有項目都屬於該用戶
        List<Long> unauthorizedIds = itemsToDelete.stream()
            .filter(item -> !item.getCartId().equals(cart.getId()))
            .map(CartItem::getId)
            .toList();
        if (!unauthorizedIds.isEmpty()) {
            throw new RuntimeException("無權操作購物車項目: " + unauthorizedIds);
        }
        cartItemRepository.deleteAllById(cartItemIds);
        log.info("用戶 {} 批量刪除購物車項目，數量：{}", userId, cartItemIds.size());
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse removeCheckedItems(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> checkedItems = cartItemRepository.findByCartIdAndChecked(cart.getId(), true);

        if (!checkedItems.isEmpty()) {
            cartItemRepository.deleteAll(checkedItems);
            log.info("用戶 {} 刪除已勾選項目，數量：{}", userId, checkedItems.size());
        }

        return buildCartResponse(cart);
    }

    @Override
    public CartResponse getCheckedItems(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> checkedItems = cartItemRepository.findByCartIdAndChecked(cart.getId(), true);

        List<CartItemResponse> itemResponses = checkedItems.stream()
                .map(this::buildCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalItems(checkedItems.size())
                .totalAmount(totalAmount)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    @Override
    public Integer getCartItemCount(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return 0;
        }
        return Math.toIntExact(cartItemRepository.countByCartId(cart.getId()));
    }

    @Override
    public CartResponse validateCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        boolean hasInvalidItems = false;
        StringBuilder errorMessage = new StringBuilder();

        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).orElse(null);

            // 檢查商品是否存在
            if (product == null) {
                hasInvalidItems = true;
                errorMessage.append("商品不存在（ID: ").append(cartItem.getProductId()).append("）; ");
                continue;
            }

            // 檢查商品是否已下架
            if (product.getDeletedAt() != null) {
                hasInvalidItems = true;
                errorMessage.append("商品已下架：").append(product.getName()).append("; ");
                // 自動取消勾選已下架商品
                if (cartItem.getChecked()) {
                    cartItem.setChecked(false);
                    cartItemRepository.save(cartItem);
                }
                continue;
            }

            // 檢查庫存是否充足
            if (product.getStock() < cartItem.getQuantity()) {
                hasInvalidItems = true;
                errorMessage.append("商品庫存不足：").append(product.getName())
                        .append("（需要 ").append(cartItem.getQuantity())
                        .append("，庫存 ").append(product.getStock()).append("）; ");
                // 自動調整數量為庫存數量
                if (product.getStock() > 0) {
                    cartItem.setQuantity(product.getStock());
                    cartItemRepository.save(cartItem);
                } else {
                    // 庫存為0時取消勾選
                    cartItem.setChecked(false);
                    cartItemRepository.save(cartItem);
                }
            }
        }

        CartResponse response = buildCartResponse(cart);

        if (hasInvalidItems) {
            log.warn("用戶 {} 的購物車驗證發現問題：{}", userId, errorMessage);
            throw new RuntimeException("購物車驗證失敗：" + errorMessage.toString());
        }

        log.info("用戶 {} 的購物車驗證通過", userId);
        return response;
    }

    /**
     * 通過 email 獲取用戶 ID
     */
    private Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用戶不存在"));
        return user.getId();
    }

    /**
     * 獲取或創建購物車
     */
    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * 構建購物車響應
     */
    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = cartItems.stream()
                .map(this::buildCartItemResponse)
                .collect(Collectors.toList());

        BigDecimal totalAmount = itemResponses.stream()
                .filter(CartItemResponse::getChecked)
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalItems(cartItems.size())
                .totalAmount(totalAmount)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    /**
     * 構建購物車項目響應
     */
    private CartItemResponse buildCartItemResponse(CartItem cartItem) {
        Product product = productRepository.findById(cartItem.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()));

        // 檢查商品狀態
        Boolean productDeleted = product.getDeletedAt() != null;
        Boolean stockInsufficient = product.getStock() < cartItem.getQuantity();
        String errorMessage = null;

        if (productDeleted) {
            errorMessage = "商品已下架";
        } else if (stockInsufficient) {
            errorMessage = "庫存不足（剩餘 " + product.getStock() + " 件）";
        }

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productDescription(product.getDescription())
                .productPrice(product.getPrice())
                .productImageUrl(product.getImageUrl())
                .productStock(product.getStock())
                .quantity(cartItem.getQuantity())
                .checked(cartItem.getChecked())
                .subtotal(subtotal)
                .createdAt(cartItem.getCreatedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .productDeleted(productDeleted)
                .stockInsufficient(stockInsufficient)
                .errorMessage(errorMessage)
                .build();
    }
}
