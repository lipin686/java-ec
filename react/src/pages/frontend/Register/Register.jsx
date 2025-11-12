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
  PersonAdd as PersonAddIcon,
  Email as EmailIcon,
  AdminPanelSettings as AdminIcon
} from '@mui/icons-material';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../../../services/frontend/authService.js';
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
  confirmPassword: yup
    .string()
    .required('請確認密碼')
    .oneOf([yup.ref('password')], '密碼確認不一致'),
});

const Register = () => {
  const navigate = useNavigate();

  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: {
      email: '',
      password: '',
      confirmPassword: ''
    }
  });

  const onSubmit = async (data) => {
    try {
      const registerData = {
        email: data.email,
        password: data.password
      };

      const result = await authService.register(registerData);

      if (result.success) {
        toast.success('註冊成功！請使用您的郵箱和密碼登入。');
        navigate('/login');
      } else {
        toast.error(result.message || '註冊失敗');
      }
    } catch (error) {
      toast.error(error.message || '註冊失敗，請稍後再試');
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
        background: 'linear-gradient(135deg, #52c41a 0%, #389e0d 100%)',
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
          maxWidth: 420,
          width: '100%',
          boxShadow: '0 20px 40px rgba(0,0,0,0.15)',
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
                bgcolor: 'success.main',
                width: 56,
                height: 56
              }}
            >
              <PersonAddIcon fontSize="large" />
            </Avatar>
            <Typography variant="h4" component="h1" gutterBottom>
              用戶註冊
            </Typography>
            <Typography variant="body2" color="text.secondary">
              創建您的新帳戶
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
                    startAdornment: <EmailIcon sx={{ mr: 1, color: 'action.active' }} />,
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

            <Button
              type="submit"
              fullWidth
              variant="contained"
              disabled={isSubmitting}
              sx={{
                mt: 3,
                mb: 2,
                py: 1.5,
                bgcolor: 'success.main',
                '&:hover': {
                  bgcolor: 'success.dark',
                }
              }}
              startIcon={isSubmitting ? <CircularProgress size={20} /> : <PersonAddIcon />}
            >
              {isSubmitting ? '註冊中...' : '創建帳戶'}
            </Button>
          </Box>

          <Divider sx={{ my: 2 }}>其他選項</Divider>

          {/* Footer Links */}
          <Box sx={{ textAlign: 'center', mt: 2 }}>
            <Typography variant="body2" sx={{ mb: 1 }}>
              已有帳戶？
              <MuiLink component={Link} to="/login" sx={{ ml: 1 }}>
                立即登入
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

export default Register;
