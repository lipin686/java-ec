import React from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Avatar,
  Divider,
  Link as MuiLink,
  CircularProgress
} from '@mui/material';
import {
  Login as LoginIcon,
  Person as PersonIcon,
  AdminPanelSettings as AdminIcon
} from '@mui/icons-material';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useForm, Controller } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import toast from 'react-hot-toast';

// 表單驗證規則
const schema = yup.object({
  email: yup
    .string()
    .required('請輸入郵箱')
    .email('請輸入有效的郵箱格式'),
  password: yup
    .string()
    .required('請輸入密碼')
    .min(6, '密碼長度不能少於6位'),
});

const Login = () => {
  const { login } = useAuth();
  const navigate = useNavigate();

  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      email: '',
      password: ''
    }
  });

  const onSubmit = async (data) => {
    try {
      const result = await login(data);

      if (result.success) {
        toast.success('登入成功！歡迎回來！');
        navigate('/dashboard');
      } else {
        toast.error(result.message || '登入失敗');
      }
    } catch (error) {
      toast.error('登入失敗，請稍後再試');
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        padding: 3,
        width: '100vw',
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0
      }}
    >
      <Card
        sx={{
          maxWidth: 400,
          width: '100%',
          boxShadow: '0 20px 40px rgba(0,0,0,0.1)',
          borderRadius: 3
        }}
      >
        <CardContent sx={{ p: 4 }}>
          {/* Header */}
          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <Avatar
              sx={{
                mx: 'auto',
                mb: 2,
                bgcolor: 'primary.main',
                width: 56,
                height: 56
              }}
            >
              <LoginIcon fontSize="large" />
            </Avatar>
            <Typography variant="h4" component="h1" gutterBottom>
              用戶登入
            </Typography>
            <Typography variant="body2" color="text.secondary">
              歡迎回來！請登入您的帳戶
            </Typography>
          </Box>

          {/* Form */}
          <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 2 }}>
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
                  InputProps={{
                    startAdornment: <PersonIcon sx={{ mr: 1, color: 'action.active' }} />,
                  }}
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

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={isSubmitting}
              sx={{
                mt: 3,
                mb: 2,
                py: 1.5,
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                '&:hover': {
                  background: 'linear-gradient(135deg, #5a6fd8 0%, #6b4190 100%)',
                }
              }}
              startIcon={isSubmitting ? <CircularProgress size={20} /> : <LoginIcon />}
            >
              {isSubmitting ? '登入中...' : '登入'}
            </Button>
          </Box>

          <Divider sx={{ my: 2 }}>其他選項</Divider>

          {/* Footer Links */}
          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2" sx={{ mb: 1 }}>
              還沒有帳戶？
              <MuiLink component={Link} to="/register" sx={{ ml: 1 }}>
                立即註冊
              </MuiLink>
            </Typography>

            <Button
              component={Link}
              to="/admin/login"
              variant="outlined"
              fullWidth
              startIcon={<AdminIcon />}
              sx={{ mt: 2 }}
            >
              🔐 後台管理系統
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default Login;
