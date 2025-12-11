import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  IconButton,
  Card,
  CardContent,
  CardMedia,
  Grid,
  Divider,
  TextField,
  Alert,
  CircularProgress,
  AppBar,
  Toolbar,
  Stack,
  Chip,
  Badge,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Checkbox,
  FormControlLabel
} from '@mui/material';
import {
  Delete as DeleteIcon,
  Add as AddIcon,
  Remove as RemoveIcon,
  ShoppingCart as ShoppingCartIcon,
  ArrowBack as ArrowBackIcon,
  DeleteSweep as ClearCartIcon,
  Dashboard as DashboardIcon,
  Login as LoginIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useCart } from '../../../context/CartContext';
import './Cart.css';

const Cart = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const {
    cart,
    loading,
    updateCartItem,
    removeCartItem,
    clearCart,
    fetchCart,
    toggleCartItemChecked,
    toggleAllCartItems
  } = useCart();

  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [clearDialogOpen, setClearDialogOpen] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/cart' } });
    } else {
      fetchCart();
    }
  }, [isAuthenticated]);

  const handleQuantityChange = async (itemId, newQuantity) => {
    if (newQuantity < 1) return;

    try {
      setError(null);
      await updateCartItem(itemId, newQuantity);
      // 不顯示成功提示，讓操作更流暢
    } catch (err) {
      setError(err.message || '更新失敗');
    }
  };

  const handleRemoveItem = async (itemId) => {
    try {
      setError(null);
      await removeCartItem(itemId);
      setSuccess('商品已移除');
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.message || '移除失敗');
    }
  };

  const handleClearCart = async () => {
    try {
      setError(null);
      await clearCart();
      setSuccess('購物車已清空');
      setClearDialogOpen(false);
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.message || '清空失敗');
    }
  };

  const handleToggleItemChecked = async (itemId) => {
    try {
      setError(null);
      await toggleCartItemChecked(itemId);
    } catch (err) {
      setError(err.message || '操作失敗');
    }
  };

  const handleToggleAllChecked = async (event) => {
    try {
      setError(null);
      await toggleAllCartItems(event.target.checked);
    } catch (err) {
      setError(err.message || '操作失敗');
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

  // 計算選中商品的總價和總數量
  const checkedItems = cart?.items?.filter(item => item.checked) || [];
  const totalAmount = checkedItems.reduce((sum, item) => sum + item.subtotal, 0);
  const totalQuantity = checkedItems.reduce((sum, item) => sum + item.quantity, 0);
  const allChecked = cart?.items?.length > 0 && cart?.items?.every(item => item.checked);
  const someChecked = cart?.items?.some(item => item.checked);

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
            onClick={() => navigate('/')}
            sx={{ mr: 2 }}
          >
            繼續購物
          </Button>
          <Typography variant="h4" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            <ShoppingCartIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            我的購物車
          </Typography>
          {cart?.items && cart.items.length > 0 && (
            <Button
              variant="outlined"
              color="error"
              startIcon={<ClearCartIcon />}
              onClick={() => setClearDialogOpen(true)}
            >
              清空購物車
            </Button>
          )}
        </Box>

        {/* 錯誤訊息 */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {/* 成功訊息 */}
        {success && (
          <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess(null)}>
            {success}
          </Alert>
        )}

        {/* 載入中 */}
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress />
          </Box>
        ) : !cart || !cart.items || cart.items.length === 0 ? (
          // 空購物車
          <Paper sx={{ p: 6, textAlign: 'center' }}>
            <ShoppingCartIcon sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h5" gutterBottom color="text.secondary">
              購物車是空的
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
              快去選購您喜歡的商品吧！
            </Typography>
            <Button
              variant="contained"
              size="large"
              onClick={() => navigate('/')}
              sx={{
                background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                px: 4
              }}
            >
              前往購物
            </Button>
          </Paper>
        ) : (
          // 購物車內容
          <Grid container spacing={3}>
            {/* 商品列表 */}
            <Grid item xs={12} md={8}>
              <Paper sx={{ p: 2 }}>
                {/* 全選框 */}
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 2, pb: 2, borderBottom: '1px solid #eee' }}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={allChecked}
                        indeterminate={someChecked && !allChecked}
                        onChange={handleToggleAllChecked}
                      />
                    }
                    label={<Typography variant="body1" fontWeight="bold">全選</Typography>}
                  />
                  <Typography variant="body2" color="text.secondary" sx={{ ml: 2 }}>
                    已選 {checkedItems.length} / {cart.items.length} 件商品
                  </Typography>
                </Box>

                {cart.items.map((item) => (
                  <Box key={item.id}>
                    <Card sx={{ display: 'flex', mb: 2, boxShadow: 'none' }}>
                      {/* 勾選框 */}
                      <Box sx={{ display: 'flex', alignItems: 'center', pl: 2 }}>
                        <Checkbox
                          checked={item.checked}
                          onChange={() => handleToggleItemChecked(item.id)}
                        />
                      </Box>

                      <CardMedia
                        component="img"
                        sx={{ width: 120, height: 120, objectFit: 'cover' }}
                        image={getImageUrl(item.productImageUrl)}
                        alt={item.productName}
                      />
                      <Box sx={{ display: 'flex', flexDirection: 'column', flexGrow: 1 }}>
                        <CardContent sx={{ flex: '1 0 auto', pb: 1 }}>
                          <Typography variant="h6" gutterBottom>
                            {item.productName}
                          </Typography>
                          <Typography variant="body2" color="text.secondary" gutterBottom>
                            單價: {formatPrice(item.productPrice)}
                          </Typography>
                          <Typography variant="h6" color="primary">
                            小計: {formatPrice(item.subtotal)}
                          </Typography>
                        </CardContent>
                        <Box sx={{ display: 'flex', alignItems: 'center', pl: 2, pb: 2 }}>
                          <Box sx={{ display: 'flex', alignItems: 'center', mr: 2 }}>
                            <IconButton
                              size="small"
                              onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                              disabled={item.quantity <= 1}
                            >
                              <RemoveIcon />
                            </IconButton>
                            <TextField
                              value={item.quantity}
                              size="small"
                              sx={{ width: 60, mx: 1 }}
                              inputProps={{
                                style: { textAlign: 'center' },
                                readOnly: true
                              }}
                            />
                            <IconButton
                              size="small"
                              onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                            >
                              <AddIcon />
                            </IconButton>
                          </Box>
                          <IconButton
                            color="error"
                            onClick={() => handleRemoveItem(item.id)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Box>
                      </Box>
                    </Card>
                    <Divider sx={{ mb: 2 }} />
                  </Box>
                ))}
              </Paper>
            </Grid>

            {/* 訂單摘要 */}
            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3, position: 'sticky', top: 20 }}>
                <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                  訂單摘要
                </Typography>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body1">已選商品:</Typography>
                    <Typography variant="body1">{checkedItems.length} 件</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body1">商品數量:</Typography>
                    <Typography variant="body1">{totalQuantity} 件</Typography>
                  </Box>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body1">小計:</Typography>
                    <Typography variant="body1">{formatPrice(totalAmount)}</Typography>
                  </Box>
                </Box>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 3 }}>
                  <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                    總計:
                  </Typography>
                  <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
                    {formatPrice(totalAmount)}
                  </Typography>
                </Box>
                <Button
                  variant="contained"
                  fullWidth
                  size="large"
                  disabled={checkedItems.length === 0}
                  sx={{
                    background: checkedItems.length === 0
                      ? 'grey'
                      : 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    py: 1.5,
                    fontSize: '1.1rem',
                    fontWeight: 'bold'
                  }}
                  onClick={() => {
                    if (checkedItems.length === 0) {
                      setError('請至少選擇一件商品');
                      return;
                    }
                    navigate('/checkout');
                  }}
                >
                  前往結帳 ({checkedItems.length})
                </Button>
              </Paper>
            </Grid>
          </Grid>
        )}
      </Container>

      {/* 清空購物車確認對話框 */}
      <Dialog
        open={clearDialogOpen}
        onClose={() => setClearDialogOpen(false)}
      >
        <DialogTitle>確認清空購物車</DialogTitle>
        <DialogContent>
          <Typography>
            確定要清空購物車嗎？此操作無法復原。
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setClearDialogOpen(false)}>
            取消
          </Button>
          <Button onClick={handleClearCart} color="error" variant="contained">
            確認清空
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Cart;
