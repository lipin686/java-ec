import React, { useEffect, useState } from 'react';
import {
  Box,
  Container,
  Typography,
  Grid,
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Button,
  AppBar,
  Toolbar,
  Chip,
  CircularProgress,
  Alert,
  Stack
} from '@mui/material';
import {
  Login as LoginIcon,
  PersonAdd as RegisterIcon,
  Visibility as VisibilityIcon,
  Dashboard as DashboardIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import productService from '../../../services/frontend/productService';
import './Home.css';

const Home = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await productService.getAvailableProducts();
      setProducts(response.data || []);
    } catch (err) {
      setError(err.message || '獲取商品列表失敗');
    } finally {
      setLoading(false);
    }
  };

  const handleProductClick = (productId) => {
    navigate(`/products/${productId}`);
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('zh-TW', {
      style: 'currency',
      currency: 'TWD',
      minimumFractionDigits: 0
    }).format(price);
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
              <>
                <Button
                  color="inherit"
                  startIcon={<LoginIcon />}
                  onClick={() => navigate('/login', { state: { from: '/' } })}
                >
                  登入
                </Button>
                <Button
                  color="inherit"
                  startIcon={<RegisterIcon />}
                  onClick={() => navigate('/register')}
                >
                  註冊
                </Button>
              </>
            )}
          </Stack>
        </Toolbar>
      </AppBar>

      {/* 主要內容 */}
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        {/* 頁面標題 */}
        <Box sx={{ mb: 4, textAlign: 'center' }}>
          <Typography variant="h3" component="h1" gutterBottom sx={{ fontWeight: 'bold' }}>
            熱門商品
          </Typography>
          <Typography variant="h6" color="text.secondary">
            探索我們精選的優質商品
          </Typography>
        </Box>

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
        ) : (
          <>
            {/* 商品列表 */}
            {products.length === 0 ? (
              <Alert severity="info">目前沒有可用商品</Alert>
            ) : (
              <Grid container spacing={3}>
                {products.map((product) => (
                  <Grid item xs={12} sm={6} md={4} key={product.id}>
                    <Card
                      sx={{
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        transition: 'transform 0.2s, box-shadow 0.2s',
                        '&:hover': {
                          transform: 'translateY(-8px)',
                          boxShadow: '0 8px 24px rgba(0,0,0,0.15)'
                        }
                      }}
                    >
                      {/* 商品圖片 */}
                      <CardMedia
                        component="img"
                        height="200"
                        image={
                          product.imageUrl
                            ? `http://localhost:8080${product.imageUrl}`
                            : 'https://via.placeholder.com/300x200?text=No+Image'
                        }
                        alt={product.name}
                        sx={{ objectFit: 'cover' }}
                      />

                      {/* 商品資訊 */}
                      <CardContent sx={{ flexGrow: 1 }}>
                        <Typography
                          gutterBottom
                          variant="h6"
                          component="h2"
                          sx={{
                            fontWeight: 'bold',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap'
                          }}
                        >
                          {product.name}
                        </Typography>

                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{
                            mb: 2,
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            display: '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical'
                          }}
                        >
                          {product.description || '暫無描述'}
                        </Typography>

                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                          <Typography variant="h5" color="primary" sx={{ fontWeight: 'bold' }}>
                            {formatPrice(product.price)}
                          </Typography>
                          <Chip
                            label={`庫存 ${product.stock}`}
                            size="small"
                            color={product.stock > 10 ? 'success' : product.stock > 0 ? 'warning' : 'error'}
                          />
                        </Box>

                        {product.productNo && (
                          <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                            商品編號: {product.productNo}
                          </Typography>
                        )}
                      </CardContent>

                      {/* 操作按鈕 */}
                      <CardActions sx={{ p: 2, pt: 0 }}>
                        <Button
                          fullWidth
                          variant="contained"
                          startIcon={<VisibilityIcon />}
                          onClick={() => handleProductClick(product.id)}
                          sx={{
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            '&:hover': {
                              background: 'linear-gradient(135deg, #5568d3 0%, #6a4193 100%)'
                            }
                          }}
                        >
                          查看詳情
                        </Button>
                      </CardActions>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            )}
          </>
        )}
      </Container>
    </Box>
  );
};

export default Home;

