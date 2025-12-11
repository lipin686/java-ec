import api from '../api';

const orderService = {
  // 创建订单
  createOrder: async (orderData) => {
    try {
      const response = await api.post('/api/v1/orders', orderData);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 获取当前用户的所有订单
  getMyOrders: async (params = {}) => {
    try {
      const response = await api.get('/api/v1/orders', { params });
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 获取订单详情
  getOrderById: async (orderId) => {
    try {
      const response = await api.get(`/api/v1/orders/${orderId}`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 取消订单
  cancelOrder: async (orderId) => {
    try {
      const response = await api.patch(`/api/v1/orders/${orderId}/cancel`);
      return response;
    } catch (error) {
      throw error;
    }
  },

  // 获取订单统计
  getOrderStats: async () => {
    try {
      const response = await api.get('/api/v1/orders/stats');
      return response;
    } catch (error) {
      throw error;
    }
  }
};

export default orderService;

