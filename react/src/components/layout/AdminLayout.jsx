import React from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import './AdminLayout.css';
import { AppBar, Toolbar, Typography, Box } from '@mui/material';

const menuItems = [
    { path: '/admin/dashboard', label: '系統總覽' },
    { path: '/admin/users', label: '用戶管理' },
    { path: '/admin/admins', label: '管理員' },
    { path: '/admin/products', label: '商品管理' },
];

const drawerWidth = 220;

const AdminLayout = () => {
    const location = useLocation();
    return (
        <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
            <AppBar position="fixed" color="primary" sx={{ zIndex: 1201 }}>
                <Toolbar>
                    <Typography variant="h6" noWrap component="div">
                        ⚙️ 後台管理系統
                    </Typography>
                </Toolbar>
            </AppBar>
            <Box sx={{ display: 'flex', flex: 1, pt: '64px' }}>
                <Box
                    className="admin-sidebar"
                    sx={{
                        width: drawerWidth,
                        flexShrink: 0,
                        bgcolor: 'background.paper',
                        borderRight: 1,
                        borderColor: 'divider',
                        minHeight: 'calc(100vh - 64px)',
                        position: 'fixed', // 讓側邊欄固定
                        top: 64, // 固定在 AppBar 下方
                        left: 0,
                        height: 'calc(100vh - 64px)',
                        zIndex: 1100
                    }}
                >
                    <nav>
                        <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
                            {menuItems.map(item => (
                                <li key={item.path} className={location.pathname.startsWith(item.path) ? 'active' : ''}>
                                    <Link to={item.path} style={{ display: 'block', padding: '16px', color: 'inherit', textDecoration: 'none' }}>{item.label}</Link>
                                </li>
                            ))}
                        </ul>
                    </nav>
                </Box>
                <Box component="main" sx={{ flex: 1, p: 3, bgcolor: '#f7f7f7', minHeight: 'calc(100vh - 64px)', marginLeft: `${drawerWidth}px` }}>
                    <Outlet />
                </Box>
            </Box>
        </Box>
    );
};

export default AdminLayout;
