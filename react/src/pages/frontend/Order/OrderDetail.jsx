import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  CardContent,
  CardMedia,
  Grid,
  Chip,
  CircularProgress,
  AppBar,
  Toolbar,
  Stack,
  Alert,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stepper,
  Step,
  StepLabel
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon,
  Receipt as ReceiptIcon,
  Cancel as CancelIcon,
  Dashboard as DashboardIcon,
  Login as LoginIcon
} from '@mui/icons-material';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import orderService from '../../../services/frontend/orderService';
import './OrderDetail.css';

const OrderDetail = () => {
  const navigate = useNavigate();
  const { orderId } = useParams();
  const { isAuthenticated, user } = useAuth();

  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: `/orders/${orderId}` } });
    } else {
      fetchOrderDetail();
    }
  }, [isAuthenticated, orderId]);

  const fetchOrderDetail = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await orderService.getOrderById(orderId);
      setOrder(response.data);
    } catch (err) {
      setError(err.message || '載入訂單詳情失敗');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async () => {
    try {
      setError(null);
      await orderService.cancelOrder(orderId);
      setSuccess('訂單已取消');
      setCancelDialogOpen(false);
      fetchOrderDetail();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.message || '取消訂單失敗');
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('zh-TW', {
      style: 'currency',
      currency: 'TWD',
      minimumFractionDigits: 0
    }).format(price);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('zh-TW', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getStatusColor = (status) => {
    const statusColors = {
      PENDING: 'warning',
      PAID: 'info',
      PROCESSING: 'primary',
      SHIPPED: 'secondary',
      DELIVERED: 'success',
      CANCELLED: 'error',
      REFUNDED: 'default',
      CONFIRMED: 'info',
      COMPLETED: 'success'
    };
    return statusColors[status] || 'default';
  };

  const getStatusText = (status) => {
    const statusTexts = {
      PENDING: '待處理',
      CONFIRMED: '已確認',
      PAID: '已付款',
      PROCESSING: '處理中',
      SHIPPED: '已出貨',
      DELIVERED: '已送達',
      COMPLETED: '已完成',
      CANCELLED: '已取消',
      REFUNDED: '已退款'
    };
    return statusTexts[status] || status;
  };

  const canCancelOrder = (status) => {
    return ['PENDING', 'CONFIRMED', 'PAID'].includes(status);
  };

  const getImageUrl = (imagePath) => {
    if (!imagePath) return '/images/products/default.jpg';
    if (imagePath.startsWith('http')) return imagePath;
    return `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}${imagePath}`;
  };

  const getOrderSteps = (status) => {
    const steps = [
      { label: '訂單建立', status: 'PENDING' },
      { label: '已確認', status: 'CONFIRMED' },
      { label: '處理中', status: 'PROCESSING' },
      { label: '已出貨', status: 'SHIPPED' },
      { label: '已送達', status: 'DELIVERED' }
    ];

    if (status === 'CANCELLED') {
      return [{ label: '訂單已取消', status: 'CANCELLED' }];
    }

    const statusOrder = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'COMPLETED'];
    const currentIndex = statusOrder.indexOf(status);

    return steps.map((step, index) => ({
      ...step,
      active: index <= currentIndex
    }));
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
            onClick={() => navigate('/orders')}
            sx={{ mr: 2 }}
          >
            返回訂單列表
          </Button>
          <Typography variant="h4" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            <ReceiptIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            訂單詳情
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
          <Alert severity="success" sx={{ mb: 3 }} onClose={() => setSuccess(null)}>
            {success}
          </Alert>
        )}

        {/* 載入中 */}
        {loading ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}>
            <CircularProgress />
          </Box>
        ) : !order ? (
          <Paper sx={{ p: 6, textAlign: 'center' }}>
            <Typography variant="h5" color="text.secondary">
              找不到訂單
            </Typography>
          </Paper>
        ) : (
          <Grid container spacing={3}>
            {/* 左側：訂單資訊 */}
            <Grid item xs={12} md={8}>
              {/* 訂單狀態進度 */}
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
                  訂單狀態
                </Typography>
                {order.status !== 'CANCELLED' ? (
                  <Stepper
                    activeStep={
                      ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'].indexOf(order.status)
                    }
                    alternativeLabel
                  >
                    {getOrderSteps(order.status).map((step) => (
                      <Step key={step.label}>
                        <StepLabel>{step.label}</StepLabel>
                      </Step>
                    ))}
                  </Stepper>
                ) : (
                  <Box sx={{ textAlign: 'center', py: 2 }}>
                    <Chip
                      label="訂單已取消"
                      color="error"
                      size="large"
                      sx={{ fontWeight: 'bold', fontSize: '1.1rem', py: 2, px: 3 }}
                    />
                  </Box>
                )}
              </Paper>

              {/* 訂單商品列表 */}
              <Paper sx={{ p: 3, mb: 3 }}>
                <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold', mb: 2 }}>
                  訂單商品
                </Typography>
                <TableContainer>
                  <Table>
                    <TableHead>
                      <TableRow>
                        <TableCell>商品</TableCell>
                        <TableCell align="center">單價</TableCell>
                        <TableCell align="center">數量</TableCell>
                        <TableCell align="right">小計</TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {order.orderItems?.map((item) => (
                        <TableRow key={item.id}>
                          <TableCell>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                              <CardMedia
                                component="img"
                                sx={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 1, mr: 2 }}
                                image={getImageUrl(item.productImage)}
                                alt={item.productName}
                              />
                              <Typography>{item.productName}</Typography>
                            </Box>
                          </TableCell>
                          <TableCell align="center">{formatPrice(item.price)}</TableCell>
                          <TableCell align="center">{item.quantity}</TableCell>
                          <TableCell align="right" sx={{ fontWeight: 'bold' }}>
                            {formatPrice(item.subtotal)}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Paper>

              {/* 收貨信息 */}
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                  收貨信息
                </Typography>
                <Divider sx={{ my: 2 }} />
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      收貨人
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                      {order.receiverName || '-'}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      聯絡電話
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                      {order.receiverPhone || '-'}
                    </Typography>
                  </Grid>
                  <Grid item xs={12}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      收貨地址
                    </Typography>
                    <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                      {order.receiverAddress || '-'}
                    </Typography>
                  </Grid>
                  {order.remark && (
                    <Grid item xs={12}>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        訂單備註
                      </Typography>
                      <Typography variant="body1">
                        {order.remark}
                      </Typography>
                    </Grid>
                  )}
                </Grid>
              </Paper>
            </Grid>

            {/* 右側：訂單摘要 */}
            <Grid item xs={12} md={4}>
              <Paper sx={{ p: 3, mb: 3, position: 'sticky', top: 20 }}>
                <Typography variant="h6" gutterBottom sx={{ fontWeight: 'bold' }}>
                  訂單摘要
                </Typography>
                <Divider sx={{ my: 2 }} />

                <Box sx={{ mb: 2 }}>
                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    訂單編號
                  </Typography>
                  <Typography variant="body1" sx={{ fontWeight: 'bold', mb: 2 }}>
                    {order.orderNumber}
                  </Typography>

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    訂單狀態
                  </Typography>
                  <Chip
                    label={getStatusText(order.status)}
                    color={getStatusColor(order.status)}
                    sx={{ fontWeight: 'bold', mb: 2 }}
                  />

                  <Typography variant="body2" color="text.secondary" gutterBottom>
                    下單時間
                  </Typography>
                  <Typography variant="body1" sx={{ mb: 2 }}>
                    {formatDate(order.createdAt)}
                  </Typography>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Box sx={{ mb: 2 }}>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                    <Typography variant="body1">商品總額:</Typography>
                    <Typography variant="body1">{formatPrice(order.totalAmount)}</Typography>
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
                    {formatPrice(order.totalAmount)}
                  </Typography>
                </Box>

                {canCancelOrder(order.status) && (
                  <Button
                    variant="outlined"
                    color="error"
                    fullWidth
                    startIcon={<CancelIcon />}
                    onClick={() => setCancelDialogOpen(true)}
                  >
                    取消訂單
                  </Button>
                )}
              </Paper>
            </Grid>
          </Grid>
        )}
      </Container>

      {/* 取消訂單確認對話框 */}
      <Dialog
        open={cancelDialogOpen}
        onClose={() => setCancelDialogOpen(false)}
      >
        <DialogTitle>確認取消訂單</DialogTitle>
        <DialogContent>
          <Typography>
            確定要取消此訂單嗎？此操作無法復原。
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>
            取消
          </Button>
          <Button onClick={handleCancelOrder} color="error" variant="contained">
            確認取消
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrderDetail;

