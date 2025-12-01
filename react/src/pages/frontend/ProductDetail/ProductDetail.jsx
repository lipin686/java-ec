import React, { useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Button,
  Paper,
  Grid,
  Chip,
  Alert,
  CircularProgress,
  AppBar,
  Toolbar,
  Stack,
  Divider,
  Card,
  CardContent,
  IconButton,
  Badge,
  Snackbar,
  TextField
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  ShoppingCart as ShoppingCartIcon,
  Login as LoginIcon,
  Dashboard as DashboardIcon,
  Inventory as InventoryIcon,
  Schedule as ScheduleIcon,
  Description as DescriptionIcon,
  Add as AddIcon,
  Remove as RemoveIcon
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import { useCart } from '../../../context/CartContext';
import productService from '../../../services/frontend/productService';
import './ProductDetail.css';

const ProductDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { isAuthenticated, user } = useAuth();
  const { cartItemCount, addToCart } = useCart();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [snackbar, setSnackbar] = useState({ open: false, message: '', severity: 'success' });

  useEffect(() => {
    if (id) {
      fetchProductDetail();
    }
  }, [id]);

  const fetchProductDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await productService.getProductDetail(id);
      setProduct(response.data);
    } catch (err) {
      setError(err.message || '獲取商品詳情失敗');
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

  const formatDateTime = (dateTime) => {
    if (!dateTime) return '無';
    return new Date(dateTime).toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
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
                <IconButton
                  color="inherit"
                  onClick={() => navigate('/cart')}
                  sx={{ mr: 1 }}
                >
                  <Badge badgeContent={cartItemCount} color="error">
                    <ShoppingCartIcon />
                  </Badge>
                </IconButton>
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
              <>
                <IconButton
                  color="inherit"
                  onClick={() => navigate('/login', { state: { from: `/products/${id}` } })}
                  sx={{ mr: 1 }}
                >
                  <ShoppingCartIcon />
                </IconButton>
                <Button
                  color="inherit"
                  startIcon={<LoginIcon />}
                  onClick={() => navigate('/login', { state: { from: `/products/${id}` } })}
                >
                  登入
                </Button>
              </>
            )}
          </Stack>
        </Toolbar>
      </AppBar>

      {/* 主要內容 */}
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        {/* 返回按鈕 */}
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate('/')}
          sx={{ mb: 3 }}
        >
          返回商品列表
        </Button>

        {/* 錯誤訊息 */}
        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {/* 載入中 */}
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress size={60} />
          </Box>
        ) : product ? (
          <Paper elevation={3} sx={{ p: 4, borderRadius: 2 }}>
            <Grid container spacing={4}>
              {/* 商品圖片 */}
              <Grid item xs={12} md={6}>
                <Box
                  component="img"
                  src={
                    product.imageUrl
                      ? `http://localhost:8080${product.imageUrl}`
                      : 'https://via.placeholder.com/500x500?text=No+Image'
                  }
                  alt={product.name}
                  sx={{
                    width: '100%',
                    height: 'auto',
                    maxHeight: '500px',
                    objectFit: 'cover',
                    borderRadius: 2,
                    boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
                  }}
                />
              </Grid>

              {/* 商品資訊 */}
              <Grid item xs={12} md={6}>
                <Box>
                  {/* 商品名稱 */}
                  <Typography variant="h3" component="h1" gutterBottom sx={{ fontWeight: 'bold' }}>
                    {product.name}
                  </Typography>

                  {/* 商品編號 */}
                  {product.productNo && (
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      商品編號: {product.productNo}
                    </Typography>
                  )}

                  <Divider sx={{ my: 2 }} />

                  {/* 價格 */}
                  <Box sx={{ mb: 3 }}>
                    <Typography variant="h4" color="primary" sx={{ fontWeight: 'bold' }}>
                      {formatPrice(product.price)}
                    </Typography>
                  </Box>

                  {/* 庫存狀態 */}
                  <Box sx={{ mb: 3 }}>
                    <Chip
                      icon={<InventoryIcon />}
                      label={`庫存: ${product.stock} 件`}
                      color={product.stock > 10 ? 'success' : product.stock > 0 ? 'warning' : 'error'}
                      sx={{ fontSize: '1rem', py: 2.5, px: 1 }}
                    />
                  </Box>

                  <Divider sx={{ my: 2 }} />

                  {/* 商品描述 */}
                  <Card sx={{ mb: 3, bgcolor: '#f9f9f9' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <DescriptionIcon sx={{ mr: 1, color: 'primary.main' }} />
                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                          商品描述
                        </Typography>
                      </Box>
                      <Typography variant="body1" color="text.secondary" sx={{ whiteSpace: 'pre-line' }}>
                        {product.description || '暫無描述'}
                      </Typography>
                    </CardContent>
                  </Card>

                  {/* 販售時間 */}
                  <Card sx={{ mb: 3, bgcolor: '#f9f9f9' }}>
                    <CardContent>
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                        <ScheduleIcon sx={{ mr: 1, color: 'primary.main' }} />
                        <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                          販售時間
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary">
                        開始時間: {formatDateTime(product.startAt)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        結束時間: {formatDateTime(product.endAt)}
                      </Typography>
                    </CardContent>
                  </Card>

                  {/* 操作按鈕 */}
                  <Stack spacing={2}>
                    {/* 數量選擇器 */}
                    {isAuthenticated && product.stock > 0 && (
                      <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                        <Typography variant="body1" sx={{ mr: 2, fontWeight: 'bold' }}>
                          數量:
                        </Typography>
                        <IconButton
                          onClick={() => setQuantity(Math.max(1, quantity - 1))}
                          disabled={quantity <= 1}
                          sx={{ border: '1px solid #ddd' }}
                        >
                          <RemoveIcon />
                        </IconButton>
                        <TextField
                          value={quantity}
                          onChange={(e) => {
                            const val = parseInt(e.target.value) || 1;
                            setQuantity(Math.min(Math.max(1, val), product.stock));
                          }}
                          size="small"
                          sx={{ width: 80, mx: 1 }}
                          inputProps={{
                            style: { textAlign: 'center' },
                            min: 1,
                            max: product.stock
                          }}
                        />
                        <IconButton
                          onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                          disabled={quantity >= product.stock}
                          sx={{ border: '1px solid #ddd' }}
                        >
                          <AddIcon />
                        </IconButton>
                      </Box>
                    )}

                    <Button
                      fullWidth
                      variant="contained"
                      size="large"
                      startIcon={<ShoppingCartIcon />}
                      disabled={product.stock === 0 || !isAuthenticated}
                      onClick={async () => {
                        if (!isAuthenticated) {
                          navigate('/login', { state: { from: `/products/${id}` } });
                          return;
                        }
                        try {
                          await addToCart(product.id, quantity);
                          setSnackbar({
                            open: true,
                            message: `成功加入 ${quantity} 件商品到購物車`,
                            severity: 'success'
                          });
                          setQuantity(1);
                        } catch (err) {
                          setSnackbar({
                            open: true,
                            message: err.message || '加入購物車失敗',
                            severity: 'error'
                          });
                        }
                      }}
                      sx={{
                        py: 1.5,
                        fontSize: '1.1rem',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        '&:hover': {
                          background: 'linear-gradient(135deg, #5568d3 0%, #6a4193 100%)'
                        }
                      }}
                    >
                      {product.stock === 0 ? '已售完' : '加入購物車'}
                    </Button>

                    {!isAuthenticated && (
                      <Alert severity="info">
                        <Typography variant="body2">
                          請先 <Button size="small" onClick={() => navigate('/login', { state: { from: `/products/${id}` } })}>登入</Button> 後才能購買商品
                        </Typography>
                      </Alert>
                    )}
                  </Stack>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        ) : (
          <Alert severity="warning">找不到該商品</Alert>
        )}
      </Container>

      {/* 提示訊息 */}
      <Snackbar
        open={snackbar.open}
        autoHideDuration={3000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert
          onClose={() => setSnackbar({ ...snackbar, open: false })}
          severity={snackbar.severity}
          sx={{ width: '100%' }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
};

export default ProductDetail;

