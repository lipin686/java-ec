import React from 'react';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Avatar,
  Alert,
  Link as MuiLink,
  CircularProgress
} from '@mui/material';
import {
  AdminPanelSettings as AdminIcon,
  Person as PersonIcon,
  ArrowBack as ArrowBackIcon
} from '@mui/icons-material';
import { useNavigate, Link } from 'react-router-dom';
import { adminService } from '../../../services/backend/adminService';
import { useFormWithSchema } from '../../../hooks/useFormWithSchema';
import { adminLoginSchema } from '../../../utils/validationSchemas';
import { storage } from '../../../utils/storage';
import toast from 'react-hot-toast';
import { Controller } from 'react-hook-form';

// AdminLogin 組件
const AdminLogin = () => {
  const navigate = useNavigate();
  const {
    control,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useFormWithSchema(adminLoginSchema, {
    email: '',
    password: ''
  });

  // 表單提交處理
  const onSubmit = async (data) => {
    try {
      const result = await adminService.login(data);

      if (result.success) {
        const { token, user } = result.data;

        // 儲存管理員資訊（直接用 localStorage）
        localStorage.setItem('adminToken', token);
        localStorage.setItem('adminUser', JSON.stringify(user));

        toast.success('後台登入成功！');
        navigate('/admin/dashboard');
      } else {
        toast.error(result.message || '登入失敗');
      }
    } catch (error) {
      toast.error(error.message || '登入失敗，請檢查您的管理員權限');
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
        background: 'linear-gradient(135deg, #2c3e50 0%, #34495e 50%, #2c3e50 100%)',
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
          maxWidth: 440,
          width: '100%',
          boxShadow: '0 25px 50px rgba(0,0,0,0.25)',
          borderRadius: 3,
          borderTop: '4px solid #e74c3c'
        }}
      >
        <CardContent sx={{ p: 4 }}>
          {/* Header */}
          <Box sx={{ textAlign: 'center', mb: 3 }}>
            <Avatar
              sx={{
                mx: 'auto',
                mb: 2,
                bgcolor: 'error.main',
                width: 64,
                height: 64
              }}
            >
              <AdminIcon fontSize="large" />
            </Avatar>
            <Typography variant="h4" component="h1" gutterBottom sx={{ color: '#2c3e50' }}>
              後台管理系統
            </Typography>
            <Typography variant="body1" color="text.secondary">
              請使用管理員帳戶登入
            </Typography>
          </Box>

          {/* Warning Alert */}
          <Alert
            severity="warning"
            sx={{ mb: 3, borderRadius: 2 }}
            icon={<AdminIcon />}
          >
            <Typography variant="body2">
              <strong>管理員專用</strong><br />
              此系統僅限具有管理員權限的用戶使用
            </Typography>
          </Alert>

          {/* Form */}
          <Box component="form" onSubmit={handleSubmit(onSubmit)} sx={{ mt: 2 }}>
            <Controller
              name="email"
              control={control}
              render={({ field }) => (
                <TextField
                  {...field}
                  fullWidth
                  label="管理員郵箱"
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
                  label="管理員密碼"
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
                bgcolor: 'error.main',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: 0.5,
                '&:hover': {
                  bgcolor: 'error.dark',
                }
              }}
              startIcon={isSubmitting ? <CircularProgress size={20} /> : <AdminIcon />}
            >
              {isSubmitting ? '登入中...' : '登入後台'}
            </Button>
          </Box>

          {/* Footer */}
          <Box sx={{ textAlign: 'center', mt: 3, pt: 2, borderTop: 1, borderColor: 'divider' }}>
            <MuiLink
              component={Link}
              to="/login"
              sx={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                textDecoration: 'none',
                '&:hover': { textDecoration: 'underline' }
              }}
            >
              <ArrowBackIcon sx={{ mr: 1 }} />
              返回前台登入
            </MuiLink>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default AdminLogin;
