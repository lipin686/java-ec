import api from './api';

// 認證相關API服務
export const authService = {
  // 用戶登入
  login: async (loginData) => {
    try {
      const response = await api.post('/api/v1/auth/login', loginData);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // 用戶註冊
  register: async (registerData) => {
    try {
      const response = await api.post('/api/v1/auth/register', registerData);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // 忘記密碼
  forgotPassword: async (forgotPasswordData) => {
    try {
      const response = await api.post('/api/v1/auth/forgot-password', forgotPasswordData);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // 檢查登入狀態
  checkLogin: async (email) => {
    try {
      const response = await api.get(`/api/v1/auth/check-login/${email}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || error.message;
    }
  },

  // 登出
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }
};
