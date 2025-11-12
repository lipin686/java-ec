import api from '../api';

export const adminService = {
    // 管理員登入
    login: async (credentials) => {
        return await api.post('/admin/v1/auth/login', credentials);
    },

    // 創建管理員
    createAdmin: async (userData) => {
        return await api.post('/admin/v1/create-admin', userData);
    },

    // 創建用戶
    createUser: async (userData) => {
        return await api.post('/admin/v1/create-user', userData);
    },

    // 獲取所有用戶
    getAllUsers: async () => {
        return await api.get('/admin/v1/users');
    },

    // 根據角色獲取用戶
    getUsersByRole: async (role) => {
        return await api.get(`/admin/v1/users/role/${role}`);
    },

    // 獲取所有管理員
    getAllAdmins: async () => {
        return await api.get('/admin/v1/admins');
    },

    // 獲取所有前台用戶
    getAllFrontendUsers: async () => {
        return await api.get('/admin/v1/frontend-users');
    },

    // 切換用戶狀態
    toggleUserStatus: async (userId) => {
        return await api.put(`/admin/v1/users/${userId}/toggle-status`);
    },

    // 軟刪除用戶
    deleteUser: async (userId) => {
        return await api.delete(`/admin/v1/users/${userId}`);
    },

    // 恢復已刪除的用戶
    restoreUser: async (userId) => {
        return await api.put(`/admin/v1/users/${userId}/restore`);
    },

    // 為用戶添加角色
    addUserRole: async (userId, role) => {
        return await api.put(`/admin/v1/users/${userId}/add-role/${role}`);
    },

    // 移除用戶角色
    removeUserRole: async (userId, role) => {
        return await api.put(`/admin/v1/users/${userId}/remove-role/${role}`);
    },

    // 獲取已刪除的用戶
    getDeletedUsers: async () => {
        return await api.get('/admin/v1/deleted-users');
    },

    // 獲取用戶統計信息
    getUserStatistics: async () => {
        return await api.get('/admin/v1/statistics');
    }
};