import React, { useState, useEffect } from 'react';
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  Container,
  Grid,
  Card,
  CardContent,
  Button,
  Avatar,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Alert
} from '@mui/material';
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  People as PeopleIcon,
  SupervisorAccount as SupervisorAccountIcon,
  Logout as LogoutIcon,
  Add as AddIcon,
  Refresh as RefreshIcon,
  Delete as DeleteIcon,
  PersonAdd as PersonAddIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { adminService } from '../../../services/adminService';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import toast from 'react-hot-toast';

const drawerWidth = 240;

// 表單驗證規則
const schema = yup.object({
  name: yup
    .string()
    .required('請輸入姓名')
    .min(2, '姓名長度不能少於2位'),
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
  const [mobileOpen, setMobileOpen] = useState(false);
  const [selectedTab, setSelectedTab] = useState('overview');
  const [adminUser, setAdminUser] = useState(null);
  const [createUserOpen, setCreateUserOpen] = useState(false);
  // 新增角色管理相關狀態
  const [roleManageOpen, setRoleManageOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [roleAction, setRoleAction] = useState('add'); // 'add' or 'remove'
  const [selectedRole, setSelectedRole] = useState('USER');
  const navigate = useNavigate();
  const queryClient = useQueryClient();

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
      return;
    }

    setAdminUser(JSON.parse(user));
  }, [navigate]);

  // React Query數據獲取
  const { data: statistics } = useQuery({
    queryKey: ['admin-statistics'],
    queryFn: () => adminService.getUserStatistics(),
    enabled: !!adminUser
  });

  const { data: users, isLoading: usersLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminService.getAllUsers(),
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

  const toggleStatusMutation = useMutation({
    mutationFn: (userId) => adminService.toggleUserStatus(userId),
    onSuccess: () => {
      toast.success('用戶狀態更新成功！');
      queryClient.invalidateQueries(['admin-users']);
      queryClient.invalidateQueries(['admin-statistics']);
    },
    onError: (error) => {
      toast.error(error.message || '操作失敗');
    }
  });

  const deleteUserMutation = useMutation({
    mutationFn: (userId) => adminService.deleteUser(userId),
    onSuccess: () => {
      toast.success('用戶刪除成功！');
      queryClient.invalidateQueries(['admin-users']);
      queryClient.invalidateQueries(['admin-statistics']);
    },
    onError: (error) => {
      toast.error(error.message || '刪除失敗');
    }
  });

  // 新增角色管理 mutations
  const addRoleMutation = useMutation({
    mutationFn: ({ userId, role }) => adminService.addUserRole(userId, role),
    onSuccess: () => {
      toast.success('角色添加成功！');
      queryClient.invalidateQueries(['admin-users']);
      queryClient.invalidateQueries(['admin-statistics']);
      setRoleManageOpen(false);
    },
    onError: (error) => {
      toast.error(error.message || '角色添加失敗');
    }
  });

  const removeRoleMutation = useMutation({
    mutationFn: ({ userId, role }) => adminService.removeUserRole(userId, role),
    onSuccess: () => {
      toast.success('角色移除成功！');
      queryClient.invalidateQueries(['admin-users']);
      queryClient.invalidateQueries(['admin-statistics']);
      setRoleManageOpen(false);
    },
    onError: (error) => {
      toast.error(error.message || '角色移除失敗');
    }
  });

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminUser');
    navigate('/admin/login');
  };

  const handleCreateUser = (data) => {
    createUserMutation.mutate(data);
  };

  // 新增角色管理處理函數
  const handleOpenRoleManage = (user, action) => {
    setSelectedUser(user);
    setRoleAction(action);
    setSelectedRole('USER');
    setRoleManageOpen(true);
  };

  const handleRoleManage = () => {
    if (!selectedUser) return;

    const mutation = roleAction === 'add' ? addRoleMutation : removeRoleMutation;
    mutation.mutate({
      userId: selectedUser.id,
      role: selectedRole
    });
  };

  const menuItems = [
    { key: 'overview', label: '系統總覽', icon: <DashboardIcon /> },
    { key: 'users', label: '用戶管理', icon: <PeopleIcon /> },
    { key: 'admins', label: '管理員', icon: <SupervisorAccountIcon /> },
  ];

  const drawer = (
    <div>
      <Toolbar>
        <Typography variant="h6" noWrap component="div" sx={{ display: 'flex', alignItems: 'center' }}>
          ⚙️ 後台管理
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem key={item.key} disablePadding>
            <ListItemButton
              selected={selectedTab === item.key}
              onClick={() => setSelectedTab(item.key)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.label} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>
    </div>
  );

  const renderContent = () => {
    switch (selectedTab) {
      case 'overview':
        return (
          <Container maxWidth="lg">
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center' }}>
                <DashboardIcon sx={{ mr: 2 }} />
                系統總覽
              </Typography>
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
                    onClick={() => setSelectedTab('users')}
                  >
                    管理用戶
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<SupervisorAccountIcon />}
                    onClick={() => setSelectedTab('admins')}
                  >
                    管理員列表
                  </Button>
                </Box>
              </CardContent>
            </Card>
          </Container>
        );

      case 'users':
        return (
          <Container maxWidth="lg">
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h4" sx={{ display: 'flex', alignItems: 'center' }}>
                <PeopleIcon sx={{ mr: 2 }} />
                用戶管理
              </Typography>
              <Box>
                <Button
                  startIcon={<RefreshIcon />}
                  onClick={() => queryClient.invalidateQueries(['admin-users'])}
                  sx={{ mr: 1 }}
                >
                  刷新
                </Button>
                <Button
                  variant="contained"
                  startIcon={<AddIcon />}
                  onClick={() => setCreateUserOpen(true)}
                >
                  創建用戶
                </Button>
              </Box>
            </Box>

            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>用戶</TableCell>
                    <TableCell>角色</TableCell>
                    <TableCell>狀態</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {users?.data?.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell>{user.id}</TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <Avatar sx={{ mr: 2, width: 32, height: 32 }}>
                            {user.email[0].toUpperCase()}
                          </Avatar>
                          {user.email}
                        </Box>
                      </TableCell>
                      <TableCell>
                        {user.roles?.map(role => (
                          <Chip
                            key={role}
                            label={role}
                            color={role === 'ADMIN' ? 'error' : 'primary'}
                            size="small"
                            sx={{ mr: 0.5 }}
                          />
                        ))}
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={user.enabled ? '啟用' : '停用'}
                          color={user.enabled ? 'success' : 'warning'}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
                          <Button
                            size="small"
                            variant="outlined"
                            color={user.enabled ? "warning" : "success"}
                            onClick={() => toggleStatusMutation.mutate(user.id)}
                          >
                            {user.enabled ? '停用' : '啟用'}
                          </Button>

                          {!user.roles?.includes('ADMIN') && (
                            <Button
                              size="small"
                              variant="outlined"
                              color="error"
                              startIcon={<DeleteIcon />}
                              onClick={() => {
                                if (window.confirm('確定要刪除此用戶嗎？')) {
                                  deleteUserMutation.mutate(user.id);
                                }
                              }}
                            >
                              刪除
                            </Button>
                          )}

                          <Button
                            size="small"
                            variant="outlined"
                            color="primary"
                            onClick={() => handleOpenRoleManage(user, 'add')}
                          >
                            添加角色
                          </Button>

                          <Button
                            size="small"
                            variant="outlined"
                            color="secondary"
                            onClick={() => handleOpenRoleManage(user, 'remove')}
                          >
                            移除角色
                          </Button>
                        </Box>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </Container>
        );

      case 'admins':
        const adminUsers = users?.data?.filter(user => user.roles?.includes('ADMIN')) || [];
        return (
          <Container maxWidth="lg">
            <Typography variant="h4" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
              <SupervisorAccountIcon sx={{ mr: 2 }} />
              管理員列表
            </Typography>

            <Grid container spacing={3}>
              {adminUsers.map(admin => (
                <Grid item xs={12} sm={6} md={4} key={admin.id}>
                  <Card>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                        <Avatar sx={{ mr: 2, bgcolor: 'error.main' }}>
                          <SupervisorAccountIcon />
                        </Avatar>
                        <Box>
                          <Typography variant="h6">{admin.email}</Typography>
                          <Chip
                            label={admin.enabled ? '啟用' : '停用'}
                            color={admin.enabled ? 'success' : 'warning'}
                            size="small"
                          />
                        </Box>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        ID: {admin.id}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        角色: {admin.roles?.join(', ')}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Container>
        );

      default:
        return null;
    }
  };

  return (
    <Box sx={{ display: 'flex' }}>
      {/* AppBar */}
      <AppBar
        position="fixed"
        sx={{ width: { sm: `calc(100% - ${drawerWidth}px)` }, ml: { sm: `${drawerWidth}px` } }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            後台管理系統
          </Typography>
          <Typography variant="body2" sx={{ mr: 2 }}>
            管理員: {adminUser?.email}
          </Typography>
          <Button color="inherit" onClick={handleLogout} startIcon={<LogoutIcon />}>
            登出
          </Button>
        </Toolbar>
      </AppBar>

      {/* Drawer */}
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: 'none', sm: 'block' },
            '& .MuiDrawer-paper': { boxSizing: 'border-box', width: drawerWidth },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      {/* Main content */}
      <Box
        component="main"
        sx={{ flexGrow: 1, p: 3, width: { sm: `calc(100% - ${drawerWidth}px)` } }}
      >
        <Toolbar />
        {renderContent()}
      </Box>

      {/* Create User Dialog */}
      <Dialog open={createUserOpen} onClose={() => setCreateUserOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>創建新用戶</DialogTitle>
        <DialogContent>
          <Box component="form" sx={{ mt: 1 }}>
            <Controller
              name="name"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="姓名"
                  error={!!errors.name}
                  helperText={errors.name?.message}
                  margin="normal"
                  placeholder="請輸入姓名"
                />
              )}
            />

            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="郵箱"
                  type="email"
                  error={!!errors.email}
                  helperText={errors.email?.message}
                  margin="normal"
                />
              )}
            />

            <Controller
              name="password"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="密碼"
                  type="password"
                  error={!!errors.password}
                  helperText={errors.password?.message}
                  margin="normal"
                />
              )}
            />

            <Controller
              name="role"
              control={control}
              render={({ field }) => (
                <FormControl fullWidth margin="normal" error={!!errors.role}>
                  <InputLabel>角色</InputLabel>
                  <Select {...field} label="角色">
                    <MenuItem value="USER">一般用戶</MenuItem>
                    <MenuItem value="ADMIN">管理員</MenuItem>
                  </Select>
                </FormControl>
              )}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateUserOpen(false)}>取消</Button>
          <Button
            onClick={handleSubmit(handleCreateUser)}
            variant="contained"
            disabled={isSubmitting}
          >
            創建
          </Button>
        </DialogActions>
      </Dialog>

      {/* Role Manage Dialog */}
      <Dialog open={roleManageOpen} onClose={() => setRoleManageOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{roleAction === 'add' ? '添加角色' : '移除角色'}</DialogTitle>
        <DialogContent>
          <Typography variant="body1" gutterBottom>
            用戶：{selectedUser?.email}
          </Typography>
          <Typography variant="body2" gutterBottom sx={{ mb: 2 }}>
            當前角色：{selectedUser?.roles?.join(', ') || '無'} ({selectedUser?.roles?.length || 0} 個角色)
          </Typography>
          <Typography variant="body1" gutterBottom>
            {roleAction === 'add' ? '選擇要添加的角色' : '選擇要移除的角色'}
          </Typography>
          <FormControl fullWidth margin="normal">
            <InputLabel>角色</InputLabel>
            <Select
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              label="角色"
            >
              <MenuItem value="USER">一般用戶</MenuItem>
              <MenuItem value="ADMIN">管理員</MenuItem>
            </Select>
          </FormControl>

          {/* 顯示操作提示 */}
          {roleAction === 'add' && selectedUser?.roles?.includes(selectedRole) && (
            <Typography variant="body2" color="warning.main" sx={{ mt: 1 }}>
              ⚠️ 用戶已經擁有此角色
            </Typography>
          )}

          {roleAction === 'remove' && !selectedUser?.roles?.includes(selectedRole) && (
            <Typography variant="body2" color="warning.main" sx={{ mt: 1 }}>
              ⚠️ 用戶沒有此角色
            </Typography>
          )}

          {roleAction === 'remove' && selectedUser?.roles?.length === 1 && (
            <Typography variant="body2" color="error.main" sx={{ mt: 1 }}>
              🚫 無法移除最後一個角色，每個用戶至少需要一個角色
            </Typography>
          )}

          {roleAction === 'remove' && selectedUser?.roles?.length === 1 && selectedUser?.roles?.includes(selectedRole) && (
            <Alert severity="error" sx={{ mt: 2 }}>
              <Typography variant="body2">
                此用戶只有一個角色，不能移除。如需更改角色，請先添加新角色，再移除舊角色。
              </Typography>
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRoleManageOpen(false)}>取消</Button>
          <Button
            onClick={handleRoleManage}
            variant="contained"
            disabled={
              (roleAction === 'add' && selectedUser?.roles?.includes(selectedRole)) ||
              (roleAction === 'remove' && !selectedUser?.roles?.includes(selectedRole)) ||
              (roleAction === 'remove' && selectedUser?.roles?.length === 1)
            }
          >
            {roleAction === 'add' ? '添加角色' : '移除角色'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminDashboard;
