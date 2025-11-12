import React, { useState } from 'react';
import {
  Box,
  Typography,
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
  CircularProgress,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  Alert
} from '@mui/material';
import { Refresh as RefreshIcon, Add as AddIcon, Delete as DeleteIcon, People as PeopleIcon } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { adminService } from '../../../services/backend/adminService';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import toast from 'react-hot-toast';

const schema = yup.object({
  name: yup.string().required('請輸入姓名').min(2, '姓名長度不能少於2位'),
  email: yup.string().required('請輸入郵箱').email('請輸入有效的郵箱格式'),
  password: yup.string().required('請輸入密碼').min(6, '密碼長度不能少於6位'),
  role: yup.string().required('請選擇角色'),
});

const UserList = () => {
  const queryClient = useQueryClient();
  const [createUserOpen, setCreateUserOpen] = useState(false);
  const [roleManageOpen, setRoleManageOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [roleAction, setRoleAction] = useState('add');
  const [selectedRole, setSelectedRole] = useState('USER');

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting }
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: { name: '', email: '', password: '', role: 'USER' }
  });

  const { data: users, isLoading } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminService.getAllUsers(),
  });

  const createUserMutation = useMutation({
    mutationFn: (userData) => userData.role === 'ADMIN' ? adminService.createAdmin(userData) : adminService.createUser(userData),
    onSuccess: () => {
      toast.success('用戶創建成功！');
      queryClient.invalidateQueries(['admin-users']);
      setCreateUserOpen(false);
      reset();
    },
    onError: (error) => toast.error(error.message || '創建失敗')
  });

  const toggleStatusMutation = useMutation({
    mutationFn: (userId) => adminService.toggleUserStatus(userId),
    onSuccess: () => {
      toast.success('用戶狀態更新成功！');
      queryClient.invalidateQueries(['admin-users']);
    },
    onError: (error) => toast.error(error.message || '操作失敗')
  });

  const deleteUserMutation = useMutation({
    mutationFn: (userId) => adminService.deleteUser(userId),
    onSuccess: () => {
      toast.success('用戶刪除成功！');
      queryClient.invalidateQueries(['admin-users']);
    },
    onError: (error) => toast.error(error.message || '刪除失敗')
  });

  const addRoleMutation = useMutation({
    mutationFn: ({ userId, role }) => adminService.addUserRole(userId, role),
    onSuccess: () => {
      toast.success('角色添加成功！');
      queryClient.invalidateQueries(['admin-users']);
      setRoleManageOpen(false);
    },
    onError: (error) => toast.error(error.message || '角色添加失敗')
  });

  const removeRoleMutation = useMutation({
    mutationFn: ({ userId, role }) => adminService.removeUserRole(userId, role),
    onSuccess: () => {
      toast.success('角色移除成功！');
      queryClient.invalidateQueries(['admin-users']);
      setRoleManageOpen(false);
    },
    onError: (error) => toast.error(error.message || '角色移除失敗')
  });

  const handleCreateUser = (data) => createUserMutation.mutate(data);
  const handleOpenRoleManage = (user, action) => {
    setSelectedUser(user);
    setRoleAction(action);
    setSelectedRole('USER');
    setRoleManageOpen(true);
  };
  const handleRoleManage = () => {
    if (!selectedUser) return;
    const mutation = roleAction === 'add' ? addRoleMutation : removeRoleMutation;
    mutation.mutate({ userId: selectedUser.id, role: selectedRole });
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <PeopleIcon sx={{ fontSize: 36, color: 'primary.main', mr: 2 }} />
        <Typography variant="h4" sx={{ fontWeight: 600 }}>用戶管理</Typography>
      </Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box />
        <Box>
          <Button startIcon={<RefreshIcon />} onClick={() => queryClient.invalidateQueries(['admin-users'])} sx={{ mr: 1 }}>刷新</Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => setCreateUserOpen(true)}>創建用戶</Button>
        </Box>
      </Box>
      {isLoading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}><CircularProgress /></Box>
      ) : (
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
                      <Avatar sx={{ mr: 2, width: 32, height: 32 }}>{user.email[0].toUpperCase()}</Avatar>
                      {user.email}
                    </Box>
                  </TableCell>
                  <TableCell>
                    {user.roles?.map(role => (
                      <Chip key={role} label={role} color={role === 'ADMIN' ? 'error' : 'primary'} size="small" sx={{ mr: 0.5 }} />
                    ))}
                  </TableCell>
                  <TableCell>
                    <Chip label={user.enabled ? '啟用' : '停用'} color={user.enabled ? 'success' : 'warning'} size="small" />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', alignItems: 'center' }}>
                      <Button size="small" variant="outlined" color={user.enabled ? "warning" : "success"} onClick={() => toggleStatusMutation.mutate(user.id)}>{user.enabled ? '停用' : '啟用'}</Button>
                      {!user.roles?.includes('ADMIN') && (
                        <Button size="small" variant="outlined" color="error" startIcon={<DeleteIcon />} onClick={() => { if (window.confirm('確定要刪除此用戶嗎？')) { deleteUserMutation.mutate(user.id); } }}>刪除</Button>
                      )}
                      <Button size="small" variant="outlined" color="primary" onClick={() => handleOpenRoleManage(user, 'add')}>添加角色</Button>
                      <Button size="small" variant="outlined" color="secondary" onClick={() => handleOpenRoleManage(user, 'remove')}>移除角色</Button>
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}
      {/* Create User Dialog */}
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
      {/* Role Manage Dialog */}
      <Dialog open={roleManageOpen} onClose={() => setRoleManageOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>{roleAction === 'add' ? '添加角色' : '移除角色'}</DialogTitle>
        <DialogContent>
          <Typography variant="body1" gutterBottom>用戶：{selectedUser?.email}</Typography>
          <Typography variant="body2" gutterBottom sx={{ mb: 2 }}>當前角色：{selectedUser?.roles?.join(', ') || '無'} ({selectedUser?.roles?.length || 0} 個角色)</Typography>
          <Typography variant="body1" gutterBottom>{roleAction === 'add' ? '選擇要添加的角色' : '選擇要移除的角色'}</Typography>
          <FormControl fullWidth margin="normal">
            <InputLabel>角色</InputLabel>
            <Select value={selectedRole} onChange={(e) => setSelectedRole(e.target.value)} label="角色">
              <MenuItem value="USER">一般用戶</MenuItem>
              <MenuItem value="ADMIN">管理員</MenuItem>
            </Select>
          </FormControl>
          {roleAction === 'add' && selectedUser?.roles?.includes(selectedRole) && (
            <Typography variant="body2" color="warning.main" sx={{ mt: 1 }}>⚠️ 用戶已經擁有此角色</Typography>
          )}
          {roleAction === 'remove' && !selectedUser?.roles?.includes(selectedRole) && (
            <Typography variant="body2" color="warning.main" sx={{ mt: 1 }}>⚠️ 用戶沒有此角色</Typography>
          )}
          {roleAction === 'remove' && selectedUser?.roles?.length === 1 && (
            <Typography variant="body2" color="error.main" sx={{ mt: 1 }}>🚫 無法移除最後一個角色，每個用戶至少需要一個角色</Typography>
          )}
          {roleAction === 'remove' && selectedUser?.roles?.length === 1 && selectedUser?.roles?.includes(selectedRole) && (
            <Alert severity="error" sx={{ mt: 2 }}>
              <Typography variant="body2">此用戶只有一個角色，不能移除。如需更改角色，請先添加新角色，再移除舊角色。</Typography>
            </Alert>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRoleManageOpen(false)}>取消</Button>
          <Button onClick={handleRoleManage} variant="contained" disabled={
            (roleAction === 'add' && selectedUser?.roles?.includes(selectedRole)) ||
            (roleAction === 'remove' && !selectedUser?.roles?.includes(selectedRole)) ||
            (roleAction === 'remove' && selectedUser?.roles?.length === 1)
          }>{roleAction === 'add' ? '添加角色' : '移除角色'}</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default UserList;
