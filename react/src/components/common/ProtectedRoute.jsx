import React from 'react';
import { Navigate } from 'react-router-dom';
import { Box, CircularProgress, Typography } from '@mui/material';
import { useAuth } from '../../context/AuthContext';

// 保護路由組件 - 需要登入才能訪問
const ProtectedRoute = ({ children }) => {
    const { isAuthenticated, loading } = useAuth();

    // 如果還在載入中，顯示載入畫面
    if (loading) {
        return (
            <Box
                sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    alignItems: 'center',
                    height: '100vh',
                    gap: 2
                }}
            >
                <CircularProgress size={40} />
                <Typography variant="h6" color="text.secondary">
                    載入中...
                </Typography>
            </Box>
        );
    }

    // 如果未登入，重定向到登入頁面
    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    // 如果已登入，渲染子組件
    return children;
};

export default ProtectedRoute;
