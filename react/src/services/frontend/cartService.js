import api from '../api';

const cartService = {
  // 獲取購物車
  getCart: async () => {
    try {
      const response = await api.get('/api/v1/cart');
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 添加商品到購物車
  addToCart: async (productId, quantity) => {
    try {
      const response = await api.post('/api/v1/cart/items', {
        productId,
        quantity
      });
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 更新購物車商品數量
  updateCartItem: async (itemId, quantity) => {
    try {
      const response = await api.put(`/api/v1/cart/items/${itemId}`, {
        quantity
      });
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 從購物車移除商品
  removeCartItem: async (itemId) => {
    try {
      const response = await api.delete(`/api/v1/cart/items/${itemId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 清空購物車
  clearCart: async () => {
    try {
      const response = await api.delete('/api/v1/cart');
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 獲取購物車商品數量
  getCartItemCount: async () => {
    try {
      const response = await api.get('/api/v1/cart/count');
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 切換單一商品的勾選狀態
  toggleCartItemChecked: async (itemId) => {
    try {
      const response = await api.patch(`/api/v1/cart/items/${itemId}/toggle`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 全選/取消全選
  toggleAllCartItems: async (checked) => {
    try {
      const response = await api.patch('/api/v1/cart/items/toggle-all', null, {
        params: { checked }
      });
      return response;
    } catch (error) {
      throw error;
    }
  }
};

export default cartService;

