import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Button,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Alert,
  Typography
} from '@mui/material';
import {
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  SupervisorAccount as SupervisorAccountIcon,
  Refresh as RefreshIcon,
  PersonAdd as PersonAddIcon
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { adminService } from '../../../services/backend/adminService';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import toast from 'react-hot-toast';

// 表單驗證規則
const schema = yup.object({
  name: yup
    .string()
    .required('請輸入姓名')
    .min(2, '姓名長度��能少於2位'),
  email: yup
    .string()
    .required('請輸入郵箱')
    .email('請輸入有效的郵箱格式'),
  password: yup
    .string()
    .required('請輸入密碼')
    .min(6, '密碼長度不能少於6位'),
  role: yup
    .string()
    .required('請選擇角色'),
});

const AdminDashboard = () => {
  const [createUserOpen, setCreateUserOpen] = useState(false);
  const [adminUser, setAdminUser] = useState(null); // 修正: 確保 adminUser 有定義
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const location = useLocation();

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting }
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      role: 'USER'
    }
  });

  useEffect(() => {
    const token = localStorage.getItem('adminToken');
    const user = localStorage.getItem('adminUser');

    if (!token || !user) {
      navigate('/admin/login');
    } else {
      setAdminUser(JSON.parse(user));
    }
  }, [navigate]);

  // React Query數據獲取
  const { data: statistics } = useQuery({
    queryKey: ['admin-statistics'],
    queryFn: () => adminService.getUserStatistics(),
    enabled: !!adminUser
  });

  // Mutations
  const createUserMutation = useMutation({
    mutationFn: (userData) => {
      if (userData.role === 'ADMIN') {
        return adminService.createAdmin(userData);
      } else {
        return adminService.createUser(userData);
      }
    },
    onSuccess: () => {
      toast.success('用戶創建成功！');
      queryClient.invalidateQueries(['admin-users']);
      queryClient.invalidateQueries(['admin-statistics']);
      setCreateUserOpen(false);
      reset();
    },
    onError: (error) => {
      toast.error(error.message || '創建失敗');
    }
  });

  const handleCreateUser = (data) => {
    createUserMutation.mutate(data);
  };

  // 根據路由顯示內容
  if (location.pathname === '/admin/dashboard') {
    // 系統總覽內容
    return (
      <Box>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <DashboardIcon sx={{ fontSize: 36, color: 'primary.main', mr: 2 }} />
          <Typography variant="h4" sx={{ fontWeight: 600 }}>系統總覽</Typography>
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Box />
          <Button
            startIcon={<RefreshIcon />}
            onClick={() => queryClient.invalidateQueries(['admin-statistics'])}
            variant="outlined"
          >
            刷新統計
          </Button>
        </Box>

        {/* 主要統計卡片 */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ height: '100%', background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="inherit" gutterBottom sx={{ opacity: 0.8 }}>
                      總用戶數
                    </Typography>
                    <Typography variant="h3" color="inherit">
                      {statistics?.data?.totalUsers || 0}
                    </Typography>
                  </Box>
                  <PeopleIcon sx={{ fontSize: 48, opacity: 0.3 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ height: '100%', background: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="inherit" gutterBottom sx={{ opacity: 0.8 }}>
                      有效用戶
                    </Typography>
                    <Typography variant="h3" color="inherit">
                      {statistics?.data?.activeUsers || 0}
                    </Typography>
                  </Box>
                  <PeopleIcon sx={{ fontSize: 48, opacity: 0.3 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ height: '100%', background: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="inherit" gutterBottom sx={{ opacity: 0.8 }}>
                      管理員數
                    </Typography>
                    <Typography variant="h3" color="inherit">
                      {statistics?.data?.adminCount || 0}
                    </Typography>
                  </Box>
                  <SupervisorAccountIcon sx={{ fontSize: 48, opacity: 0.3 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={3}>
            <Card sx={{ height: '100%', background: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)', color: 'white' }}>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="inherit" gutterBottom sx={{ opacity: 0.8 }}>
                      前台用戶
                    </Typography>
                    <Typography variant="h3" color="inherit">
                      {statistics?.data?.userCount || 0}
                    </Typography>
                  </Box>
                  <PeopleIcon sx={{ fontSize: 48, opacity: 0.3 }} />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* 詳細統計卡片 */}
        <Grid container spacing={3} sx={{ mb: 4 }}>
          <Grid item xs={12} sm={6} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      啟用用戶
                    </Typography>
                    <Typography variant="h4" color="success.main">
                      {statistics?.data?.enabledUsers || 0}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      佔有效用戶 {statistics?.data?.activeUsers > 0 ?
                        Math.round((statistics?.data?.enabledUsers || 0) / statistics?.data?.activeUsers * 100) : 0
                      }%
                    </Typography>
                  </Box>
                  <Chip label="啟用" color="success" />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      停用用戶
                    </Typography>
                    <Typography variant="h4" color="warning.main">
                      {statistics?.data?.disabledUsers || 0}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      佔有效用戶 {statistics?.data?.activeUsers > 0 ?
                        Math.round((statistics?.data?.disabledUsers || 0) / statistics?.data?.activeUsers * 100) : 0
                      }%
                    </Typography>
                  </Box>
                  <Chip label="停用" color="warning" />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6} md={4}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      已刪除用戶
                    </Typography>
                    <Typography variant="h4" color="error.main">
                      {statistics?.data?.deletedUsers || 0}
                    </Typography>
                    <Typography variant="body2" color="textSecondary">
                      佔總用戶 {statistics?.data?.totalUsers > 0 ?
                        Math.round((statistics?.data?.deletedUsers || 0) / statistics?.data?.totalUsers * 100) : 0
                      }%
                    </Typography>
                  </Box>
                  <Chip label="已刪除" color="error" />
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        {/* 系統狀態摘要 */}
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              系統狀態摘要
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={12} md={6}>
                <Alert severity="info" sx={{ mb: 2 }}>
                  <Typography variant="body2">
                    <strong>用戶活躍度：</strong>
                    {statistics?.data?.totalUsers > 0 ?
                      Math.round((statistics?.data?.activeUsers || 0) / statistics?.data?.totalUsers * 100) : 0
                    }% 的用戶處於活躍狀態
                  </Typography>
                </Alert>
              </Grid>
              <Grid item xs={12} md={6}>
                <Alert severity={
                  (statistics?.data?.disabledUsers || 0) > (statistics?.data?.enabledUsers || 0) / 2 ? 'warning' : 'success'
                }>
                  <Typography variant="body2">
                    <strong>帳號狀態：</strong>
                    {statistics?.data?.enabledUsers || 0} 個帳號啟用，
                    {statistics?.data?.disabledUsers || 0} 個帳號停用
                  </Typography>
                </Alert>
              </Grid>
            </Grid>
          </CardContent>
        </Card>

        {/* 快速操作 */}
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              快速操作
            </Typography>
            <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
              <Button
                variant="contained"
                startIcon={<PersonAddIcon />}
                onClick={() => setCreateUserOpen(true)}
              >
                創建用戶
              </Button>
              <Button
                variant="outlined"
                startIcon={<PeopleIcon />}
                onClick={() => navigate('/admin/users')}
              >
                管理用戶
              </Button>
              <Button
                variant="outlined"
                startIcon={<SupervisorAccountIcon />}
                onClick={() => navigate('/admin/admins')}
              >
                管理員列表
              </Button>
            </Box>
          </CardContent>
        </Card>
        {/* 新增：創建用戶 Dialog */}
        <Dialog open={createUserOpen} onClose={() => setCreateUserOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>創建新用戶</DialogTitle>
          <DialogContent>
            <Box component="form" sx={{ mt: 1 }}>
              <Controller name="name" control={control} render={({ field }) => (
                <TextField {...field} fullWidth label="姓名" error={!!errors.name} helperText={errors.name?.message} margin="normal" placeholder="請輸入姓名" />
              )} />
              <Controller name="email" control={control} render={({ field }) => (
                <TextField {...field} fullWidth label="郵箱" type="email" error={!!errors.email} helperText={errors.email?.message} margin="normal" placeholder="請輸入郵箱" />
              )} />
              <Controller name="password" control={control} render={({ field }) => (
                <TextField {...field} fullWidth label="密碼" type="password" error={!!errors.password} helperText={errors.password?.message} margin="normal" placeholder="請輸入密碼（至少6位）" />
              )} />
              <Controller name="role" control={control} render={({ field }) => (
                <FormControl fullWidth margin="normal" error={!!errors.role}>
                  <InputLabel>角色</InputLabel>
                  <Select {...field} label="角色">
                    <MenuItem value="USER">一般用戶</MenuItem>
                    <MenuItem value="ADMIN">管理員</MenuItem>
                  </Select>
                  {errors.role && (<Typography variant="caption" color="error" sx={{ mt: 1, ml: 2 }}>{errors.role.message}</Typography>)}
                </FormControl>
              )} />
            </Box>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setCreateUserOpen(false)}>取消</Button>
            <Button onClick={handleSubmit(handleCreateUser)} variant="contained" disabled={isSubmitting}>{isSubmitting ? '創建中...' : '創建用戶'}</Button>
          </DialogActions>
        </Dialog>
      </Box>
    );
  }
  // 其他內容請分別放到 /admin/users, /admin/admins, /admin/products 專屬頁面
  return null;
};

export default AdminDashboard;
