import React from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Container,
  Avatar,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Divider,
  CircularProgress
} from '@mui/material';
import {
  PersonAdd as PersonAddIcon,
  Save as SaveIcon
} from '@mui/icons-material';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import toast from 'react-hot-toast';
import { adminService } from '../../../services/adminService';

// 使用Yup定義表單驗證規則
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
  confirmPassword: yup
    .string()
    .required('請確認密碼')
    .oneOf([yup.ref('password')], '密碼確認不一致'),
  role: yup
    .string()
    .required('請選擇角色')
});

const CreateUserForm = () => {
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
      confirmPassword: '',
      role: 'USER'
    }
  });

  const onSubmit = async (data) => {
    try {
      const userData = {
        name: data.name,
        email: data.email,
        password: data.password,
        role: data.role
      };

      let result;
      if (data.role === 'ADMIN') {
        result = await adminService.createAdmin(userData);
      } else {
        result = await adminService.createUser(userData);
      }

      if (result.success) {
        toast.success(`${data.role === 'ADMIN' ? '管理員' : '用戶'}創建成功！`);
        reset(); // 重置表單
      } else {
        toast.error(result.message || '創建失敗');
      }
    } catch (error) {
      toast.error(error.message || '創建失敗，請稍後再試');
    }
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 4 }}>
      <Card sx={{ boxShadow: 3, borderRadius: 2 }}>
        <CardContent sx={{ p: 4 }}>
          {/* Header */}
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Avatar
              sx={{
                mx: 'auto',
                mb: 2,
                bgcolor: 'primary.main',
                width: 64,
                height: 64
              }}
            >
              <PersonAddIcon fontSize="large" />
            </Avatar>
            <Typography variant="h4" component="h1" gutterBottom>
              創建新用戶
            </Typography>
            <Typography variant="body2" color="text.secondary">
              填寫以下資訊來創建新的用戶帳戶
            </Typography>
          </Box>

          {/* Form */}
          <Box component="form" onSubmit={handleSubmit(onSubmit)}>
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
                  placeholder="請輸入郵箱"
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
                  placeholder="請輸入密碼（至少6位）"
                />
              )}
            />

            <Controller
              name="confirmPassword"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="確認密碼"
                  type="password"
                  error={!!errors.confirmPassword}
                  helperText={errors.confirmPassword?.message}
                  margin="normal"
                  placeholder="請再次輸入密碼"
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
                  {errors.role && (
                    <Typography variant="caption" color="error" sx={{ mt: 1, ml: 2 }}>
                      {errors.role.message}
                    </Typography>
                  )}
                </FormControl>
              )}
            />

            <Divider sx={{ my: 3 }} />

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={isSubmitting}
              size="large"
              startIcon={isSubmitting ? <CircularProgress size={20} /> : <SaveIcon />}
              sx={{ py: 1.5 }}
            >
              {isSubmitting ? '創建中...' : '創建用戶'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Container>
  );
};

export default CreateUserForm;
