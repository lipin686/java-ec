import React, { createContext, useContext, useState, useEffect } from 'react';
import cartService from '../services/frontend/cartService';
import { useAuth } from './AuthContext';

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(null);
  const [cartItemCount, setCartItemCount] = useState(0);
  const [loading, setLoading] = useState(false);

  // 當用戶登入狀態改變時，重新獲取購物車
  useEffect(() => {
    if (isAuthenticated) {
      fetchCart();
      fetchCartItemCount();
    } else {
      setCart(null);
      setCartItemCount(0);
    }
  }, [isAuthenticated]);

  // 獲取購物車
  const fetchCart = async () => {
    if (!isAuthenticated) return;

    try {
      setLoading(true);
      const response = await cartService.getCart();
      if (response.success) {
        setCart(response.data);
      }
    } catch (error) {
      console.error('獲取購物車失敗:', error);
    } finally {
      setLoading(false);
    }
  };

  // 獲取購物車商品數量
  const fetchCartItemCount = async () => {
    if (!isAuthenticated) return;

    try {
      const response = await cartService.getCartItemCount();
      if (response.success) {
        setCartItemCount(response.data || 0);
      }
    } catch (error) {
      console.error('獲取購物車數量失敗:', error);
    }
  };

  // 添加商品到購物車
  const addToCart = async (productId, quantity = 1) => {
    if (!isAuthenticated) {
      throw new Error('請先登入');
    }

    try {
      const response = await cartService.addToCart(productId, quantity);
      if (response.success) {
        await fetchCart();
        await fetchCartItemCount();
        return response;
      }
      throw new Error(response.message || '添加到購物車失敗');
    } catch (error) {
      console.error('添加到購物車失敗:', error);
      throw error;
    }
  };

  // 更新購物車商品數量
  const updateCartItem = async (itemId, quantity) => {
    if (!isAuthenticated) return;

    try {
      const response = await cartService.updateCartItem(itemId, quantity);
      if (response.success) {
        await fetchCart();
        await fetchCartItemCount();
        return response;
      }
      throw new Error(response.message || '更新購物車失敗');
    } catch (error) {
      console.error('更新購物車失敗:', error);
      throw error;
    }
  };

  // 從購物車移除商品
  const removeCartItem = async (itemId) => {
    if (!isAuthenticated) return;

    try {
      const response = await cartService.removeCartItem(itemId);
      if (response.success) {
        await fetchCart();
        await fetchCartItemCount();
        return response;
      }
      throw new Error(response.message || '移除商品失敗');
    } catch (error) {
      console.error('移除商品失敗:', error);
      throw error;
    }
  };

  // 清空購物車
  const clearCart = async () => {
    if (!isAuthenticated) return;

    try {
      const response = await cartService.clearCart();
      if (response.success) {
        await fetchCart();
        await fetchCartItemCount();
        return response;
      }
      throw new Error(response.message || '清空購物車失敗');
    } catch (error) {
      console.error('清空購物車失敗:', error);
      throw error;
    }
  };

  // 切換單一商品勾選狀態
  const toggleCartItemChecked = async (itemId) => {
    if (!isAuthenticated) return;

    try {
      const response = await cartService.toggleCartItemChecked(itemId);
      if (response.success) {
        await fetchCart();
        return response;
      }
      throw new Error(response.message || '切換勾選狀態失敗');
    } catch (error) {
      console.error('切換勾選狀態失敗:', error);
      throw error;
    }
  };

  // 全選/取消全選
  const toggleAllCartItems = async (checked) => {
    if (!isAuthenticated) return;

    try {
      const response = await cartService.toggleAllCartItems(checked);
      if (response.success) {
        await fetchCart();
        return response;
      }
      throw new Error(response.message || '全選操作失敗');
    } catch (error) {
      console.error('全選操作失敗:', error);
      throw error;
    }
  };

  const value = {
    cart,
    cartItemCount,
    loading,
    fetchCart,
    fetchCartItemCount,
    addToCart,
    updateCartItem,
    removeCartItem,
    clearCart,
    toggleCartItemChecked,
    toggleAllCartItems
  };

  return (
    <CartContext.Provider value={value}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart 必須在 CartProvider 內部使用');
  }
  return context;
};

