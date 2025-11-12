import api from '../api.js';

export const authService = {
    // 用戶登入
    login: async (loginData) => {
        try {
            const data = await api.post('/api/v1/auth/login', loginData);
            return data;
        } catch (error) {
            return {
                success: false,
                message: error.message || '登入失敗'
            };
        }
    },

    register: async (registerData) => {
        try {
            const data = await api.post('/api/v1/auth/register', registerData);
            return data;
        } catch (error) {
            return {
                success: false,
                message: error.message || '註冊失敗'
            };
        }
    },

    forgotPassword: async (forgotPasswordData) => {
        try {
            const data = await api.post('/api/v1/auth/forgot-password', forgotPasswordData);
            return data;
        } catch (error) {
            return {
                success: false,
                message: error.message || '請求失敗'
            };
        }
    },

    checkLogin: async (email) => {
        try {
            const data = await api.get(`/api/v1/auth/check-login/${email}`);
            return data;
        } catch (error) {
            return {
                success: false,
                message: error.message || '檢查失敗'
            };
        }
    },

    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    }
};