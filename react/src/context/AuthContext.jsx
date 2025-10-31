import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/authService';

// 創建認證上下文
const AuthContext = createContext();

// 認證提供者組件
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // 初始化時檢查本地存儲的用戶信息
  useEffect(() => {
    const token = localStorage.getItem('token');
    const userData = localStorage.getItem('user');

    if (token && userData) {
      try {
        const parsedUser = JSON.parse(userData);
        setUser(parsedUser);
        setIsAuthenticated(true);
      } catch (error) {
        console.error('解析用戶數據失敗:', error);
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }
    setLoading(false);
  }, []);

  // 登入函數
  const login = async (loginData) => {
    try {
      const response = await authService.login(loginData);

      if (response.success) {
        const { token, user: userData } = response.data;

        // 保存到本地存儲
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(userData));

        // 更新狀態
        setUser(userData);
        setIsAuthenticated(true);

        return { success: true, data: response.data };
      } else {
        throw new Error(response.message || '登入失敗');
      }
    } catch (error) {
      console.error('登入錯誤:', error);
      return {
        success: false,
        message: error.message || '登入失敗，請稍後再試'
      };
    }
  };

  // 登出函數
  const logout = () => {
    authService.logout();
    setUser(null);
    setIsAuthenticated(false);
  };

  // 上下文值
  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    logout
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

// 使用認證上下文的Hook
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth 必須在 AuthProvider 內部使用');
  }
  return context;
};
