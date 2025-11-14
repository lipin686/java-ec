import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

// 後台管理保護路由組件 - 需要管理員權限
const AdminProtectedRoute = ({ children }) => {
    const location = useLocation();
    const token = localStorage.getItem('adminToken');
    const adminUser = localStorage.getItem('adminUser');

    // 檢查是否有管理員token和用戶信息
    if (!token || !adminUser) {
        return <Navigate to="/admin/login" state={{ from: location.pathname }} replace />;
    }

    try {
        const user = JSON.parse(adminUser);
        // 檢查是否有管理員角色
        if (!user.roles || !user.roles.includes('ADMIN')) {
            return <Navigate to="/admin/login" state={{ from: location.pathname }} replace />;
        }
    } catch (error) {
        // 如果解析用戶信息失敗，清除數據並重定向
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminUser');
        return <Navigate to="/admin/login" state={{ from: location.pathname }} replace />;
    }

    return children;
};

export default AdminProtectedRoute;
