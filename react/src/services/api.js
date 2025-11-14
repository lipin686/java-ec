import axios from 'axios';

// 創建 axios 實例
const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 請求攔截器
api.interceptors.request.use(
  (config) => {
    // 公開 API 不需要 token
    const publicUrls = ['/auth/login', '/api/v1/products'];
    const isPublicUrl = publicUrls.some(url => config.url.includes(url));

    if (!isPublicUrl) {
      const token = localStorage.getItem('adminToken') || localStorage.getItem('userToken');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 響應攔截器
api.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response?.status === 401) {
      // 只有在非公開 API 時才清除 token 並跳轉
      const publicUrls = ['/api/v1/products'];
      const isPublicUrl = publicUrls.some(url => error.config.url.includes(url));

      if (!isPublicUrl) {
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminUser');
        localStorage.removeItem('userToken');
        localStorage.removeItem('user');

        // 如果當前在後台頁面，跳轉到後台登入
        if (window.location.pathname.startsWith('/admin')) {
          window.location.href = '/admin/login';
        } else {
          // 前台則跳轉到前台登入
          window.location.href = '/login';
        }
      }
    }

    const message = error.response?.data?.message || error.message || '發生未知錯誤';
    return Promise.reject(new Error(message));
  }
);

export default api;
