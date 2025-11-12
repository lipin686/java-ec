import React from 'react';
import { Typography, Box, Card, CardContent, Grid, Avatar, Chip } from '@mui/material';
import { SupervisorAccount as SupervisorAccountIcon } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { adminService } from '../../../services/backend/adminService';

const AdminList = () => {
    const { data: users, isLoading } = useQuery({
        queryKey: ['admin-users'],
        queryFn: () => adminService.getAllUsers(),
    });
    const adminUsers = users?.data?.filter(user => user.roles?.includes('ADMIN')) || [];
    return (
        <Box>
            <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <SupervisorAccountIcon sx={{ fontSize: 36, color: 'primary.main', mr: 2 }} />
                <Typography variant="h4" sx={{ fontWeight: 600 }}>管理員列表</Typography>
            </Box>
            <Grid container spacing={3}>
                {adminUsers.map(admin => (
                    <Grid item xs={12} sm={6} md={4} key={admin.id}>
                        <Card>
                            <CardContent>
                                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                    <Avatar sx={{ mr: 2, bgcolor: 'error.main' }}><SupervisorAccountIcon /></Avatar>
                                    <Box>
                                        <Typography variant="h6">{admin.email}</Typography>
                                        <Chip label={admin.enabled ? '啟用' : '停用'} color={admin.enabled ? 'success' : 'warning'} size="small" />
                                    </Box>
                                </Box>
                                <Typography variant="body2" color="text.secondary">ID: {admin.id}</Typography>
                                <Typography variant="body2" color="text.secondary">角色: {admin.roles?.join(', ')}</Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
};

export default AdminList;
