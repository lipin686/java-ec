import api from '../api';

/**
 * 前台商品服務
 */
const productService = {
  /**
   * 獲取所有可用商品列表
   */
  getAvailableProducts: async () => {
    try {
      return await api.get('/api/v1/products');
    } catch (error) {
      throw error;
    }
  },

  /**
   * 獲取商品詳情
   */
  getProductDetail: async (id) => {
    try {
      return await api.get(`/api/v1/products/${id}`);
    } catch (error) {
      throw error;
    }
  }
};

export default productService;
