import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  TextField,
  Grid,
  Divider,
  CircularProgress,
  AppBar,
  Toolbar,
  Stack,
  Alert,
  Chip,
  Card,
  CardMedia,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableRow,
  Radio,
  RadioGroup,
  FormControlLabel,
  FormControl
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Payment as PaymentIcon,
  Dashboard as DashboardIcon,
  Login as LoginIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useCart } from '../../../context/CartContext';
import orderService from '../../../services/frontend/orderService';
import './Checkout.css';

const Checkout = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const { cart } = useCart();

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // 表單數據 - 使用後端API的字段名稱
  const [formData, setFormData] = useState({
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    remark: '',
    paymentMethod: 'CREDIT_CARD'
  });

  const [formErrors, setFormErrors] = useState({});

  // 從購物車獲取已選中的商品
  const checkedItems = cart?.items?.filter(item => item.checked) || [];
  const totalAmount = checkedItems.reduce((sum, item) => sum + item.subtotal, 0);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/checkout' } });
      return;
    }

    // 如果沒有選中的商品，返回購物車
    if (checkedItems.length === 0) {
      navigate('/cart');
    }
  }, [isAuthenticated, checkedItems.length]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // 清除該欄位的錯誤
    if (formErrors[name]) {
      setFormErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const validateForm = () => {
    const errors = {};

    if (!formData.receiverName.trim()) {
      errors.receiverName = '請輸入收貨人姓名';
    }

    if (!formData.receiverPhone.trim()) {
      errors.receiverPhone = '請輸入聯絡電話';
    } else if (!/^09\d{8}$/.test(formData.receiverPhone.replace(/[-\s]/g, ''))) {
      errors.receiverPhone = '請輸入有效的手機號碼';
    }

    if (!formData.receiverAddress.trim()) {
      errors.receiverAddress = '請輸入收貨地址';
    }

    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmitOrder = async () => {
    if (!validateForm()) {
      setError('請填寫完整的收貨資訊');
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const orderData = {
        receiverName: formData.receiverName,
        receiverPhone: formData.receiverPhone,
        receiverAddress: formData.receiverAddress,
        remark: formData.remark
      };

      const response = await orderService.createOrder(orderData);

      setSuccess('訂單建立成功！');

      // 跳轉到訂單詳情頁
      setTimeout(() => {
        navigate(`/orders/${response.data.id}`);
      }, 1500);

    } catch (err) {
      setError(err.message || '建立訂單失敗');
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('zh-TW', {
      style: 'currency',
      currency: 'TWD',
      minimumFractionDigits: 0
    }).format(price);
  };

  const getImageUrl = (imagePath) => {
    if (!imagePath) return '/images/products/default.jpg';
    if (imagePath.startsWith('http')) return imagePath;
    return `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${imagePath}`;
  };

  return (
    <Box sx={{ flexGrow: 1, minHeight: '100vh', bgcolor: '#f5f5f5' }}>
      {/* 導航欄 */}
      <AppBar
        position="static"
        sx={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
        }}
      >
        <Toolbar>
          <Typography
            variant="h6"
            component="div"
            sx={{ flexGrow: 1, fontWeight: 'bold', cursor: 'pointer' }}
            onClick={() => navigate('/')}
          >
            🛍️ 購物商城
          </Typography>

          <Stack direction="row" spacing={2}>
            {isAuthenticated ? (
              <>
                <Button
                  color="inherit"
                  startIcon={<DashboardIcon />}
                  onClick={() => navigate('/dashboard')}
                >
                  會員中心
                </Button>
                <Chip
                  label={user?.email}
                  sx={{
                    bgcolor: 'rgba(255,255,255,0.2)',
                    color: 'white'
                  }}
                />
              </>
            ) : (
              <Button
                color="inherit"
                startIcon={<LoginIcon />}
                onClick={() => navigate('/login')}
              >
                登入
              </Button>
            )}
          </Stack>
        </Toolbar>
      </AppBar>

      {/* 主要內容 */}
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        {/* 頁面標題 */}
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
          <Button
            startIcon={<ArrowBackIcon />}
            onClick={() => navigate('/cart')}
            sx={{ mr: 2 }}
          >
            返回購物車
          </Button>
          <Typography variant="h4" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            <PaymentIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            確認訂單
          </Typography>
        </Box>

        {/* 錯誤訊息 */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* 成功訊息 */}
        {success && (
          <Alert severity="success" sx={{ mb: 3 }}>
            {success}
          </Alert>
        )}

        <Grid container spacing={3}>
          {/* 左側：收貨資訊 */}
          <Grid item xs={12} md={8}>
            {/* 收貨資訊表單 */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
                收貨資訊
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="收貨人姓名"
                    name="receiverName"
                    value={formData.receiverName}
                    onChange={handleInputChange}
                    error={!!formErrors.receiverName}
                    helperText={formErrors.receiverName}
                    required
                  />
                </Grid>
                <Grid item xs={12} sm={6}>
                  <TextField
                    fullWidth
                    label="聯絡電話"
                    name="receiverPhone"
                    value={formData.receiverPhone}
                    onChange={handleInputChange}
                    error={!!formErrors.receiverPhone}
                    helperText={formErrors.receiverPhone}
                    placeholder="0912345678"
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="收貨地址"
                    name="receiverAddress"
                    value={formData.receiverAddress}
                    onChange={handleInputChange}
                    error={!!formErrors.receiverAddress}
                    helperText={formErrors.receiverAddress}
                    multiline
                    rows={2}
                    required
                  />
                </Grid>
                <Grid item xs={12}>
                  <TextField
                    fullWidth
                    label="訂單備註"
                    name="remark"
                    value={formData.remark}
                    onChange={handleInputChange}
                    multiline
                    rows={3}
                    placeholder="如有特殊需求請在此說明"
                  />
                </Grid>
              </Grid>
            </Paper>

            {/* 付款方式 */}
            <Paper sx={{ p: 3, mb: 3 }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 2 }}>
                付款方式
              </Typography>
              <FormControl component="fieldset">
                <RadioGroup
                  name="paymentMethod"
                  value={formData.paymentMethod}
                  onChange={handleInputChange}
                >
                  <FormControlLabel
                    value="CREDIT_CARD"
                    control={<Radio />}
                    label="信用卡付款"
                  />
                  <FormControlLabel
                    value="ATM"
                    control={<Radio />}
                    label="ATM轉帳"
                  />
                  <FormControlLabel
                    value="COD"
                    control={<Radio />}
                    label="貨到付款"
                  />
                </RadioGroup>
              </FormControl>
            </Paper>

            {/* 訂單商品 */}
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 2 }}>
                訂單商品 ({checkedItems.length})
              </Typography>
              <TableContainer>
                <Table>
                  <TableBody>
                    {checkedItems.map((item) => (
                      <TableRow key={item.id}>
                        <TableCell sx={{ width: 80 }}>
                          <CardMedia
                            component="img"
                            sx={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 1 }}
                            image={getImageUrl(item.productImageUrl)}
                            alt={item.productName}
                          />
                        </TableCell>
                        <TableCell>
                          <Typography variant="body1">{item.productName}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            {formatPrice(item.productPrice)} x {item.quantity}
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                            {formatPrice(item.subtotal)}
                          </Typography>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </Paper>
          </Grid>

          {/* 右側：訂單摘要 */}
          <Grid item xs={12} md={4}>
            <Paper sx={{ p: 3, position: 'sticky', top: 20 }}>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                訂單摘要
              </Typography>
              <Divider sx={{ my: 2 }} />

              <Box sx={{ mb: 2 }}>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body1">商品件數:</Typography>
                  <Typography variant="body1">{checkedItems.length} 件</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body1">商品總額:</Typography>
                  <Typography variant="body1">{formatPrice(totalAmount)}</Typography>
                </Box>
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                  <Typography variant="body1">運費:</Typography>
                  <Typography variant="body1">{formatPrice(0)}</Typography>
                </Box>
              </Box>

              <Divider sx={{ my: 2 }} />

              <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                  訂單總額:
                </Typography>
                <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
                  {formatPrice(totalAmount)}
                </Typography>
              </Box>

              <Button
                variant="contained"
                fullWidth
                size="large"
                onClick={handleSubmitOrder}
                disabled={loading || checkedItems.length === 0}
                sx={{
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  py: 1.5,
                  fontSize: '1.1rem',
                  fontWeight: 'bold'
                }}
              >
                {loading ? (
                  <CircularProgress size={24} color="inherit" />
                ) : (
                  '確認下單'
                )}
              </Button>

              <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 2, textAlign: 'center' }}>
                點擊「確認下單」即表示您同意我們的服務條款
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
};

export default Checkout;

