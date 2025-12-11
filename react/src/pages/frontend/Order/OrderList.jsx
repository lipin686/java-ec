import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  Paper,
  Button,
  Card,
  CardContent,
  Grid,
  Chip,
  CircularProgress,
  AppBar,
  Toolbar,
  Stack,
  Alert,
  Divider,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Pagination
} from '@mui/material';
import {
  ShoppingBag as OrderIcon,
  ArrowBack as ArrowBackIcon,
  FilterList as FilterIcon,
  Visibility as ViewIcon,
  Cancel as CancelIcon,
  Dashboard as DashboardIcon,
  Login as LoginIcon,
  Receipt as ReceiptIcon
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../../context/AuthContext';
import orderService from '../../../services/frontend/orderService';
import './OrderList.css';

const OrderList = () => {
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  // 分页和过滤
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [filterAnchorEl, setFilterAnchorEl] = useState(null);

  // 取消订单对话框
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [selectedOrderId, setSelectedOrderId] = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login', { state: { from: '/orders' } });
    } else {
      fetchOrders();
    }
  }, [isAuthenticated, statusFilter]); // 移除 page，只在状态筛选变化时重新获取

  const fetchOrders = async () => {
    try {
      setLoading(true);
      setError(null);

      const params = {};

      if (statusFilter !== 'ALL') {
        params.status = statusFilter;
      }

      console.log('🔍 正在获取订单，参数:', params);
      const response = await orderService.getMyOrders(params);
      console.log('📦 API 完整响应:', response);
      console.log('📋 订单数据 (response.data):', response.data);

      // 后端返回的是数组，不是分页对象
      if (response.data && Array.isArray(response.data)) {
        console.log('✅ 订单数组长度:', response.data.length);
        setOrders(response.data);
        // 前端自己做简单分页
        const itemsPerPage = 10;
        const total = Math.ceil(response.data.length / itemsPerPage);
        setTotalPages(total > 0 ? total : 1);
      } else if (response && Array.isArray(response)) {
        // 如果 response 本身就是数组
        console.log('✅ response 本身是数组，长度:', response.length);
        setOrders(response);
        const itemsPerPage = 10;
        const total = Math.ceil(response.length / itemsPerPage);
        setTotalPages(total > 0 ? total : 1);
      } else {
        console.log('⚠️ 未找到订单数组');
        setOrders([]);
        setTotalPages(1);
      }
    } catch (err) {
      console.error('❌ 获取订单失败:', err);
      setError(err.message || '載入訂單失敗');
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelOrder = async () => {
    if (!selectedOrderId) return;

    try {
      setError(null);
      await orderService.cancelOrder(selectedOrderId);
      setSuccess('訂單已取消');
      setCancelDialogOpen(false);
      setSelectedOrderId(null);
      fetchOrders();
      setTimeout(() => setSuccess(null), 3000);
    } catch (err) {
      setError(err.message || '取消訂單失敗');
    }
  };

  const handleViewOrder = (orderId) => {
    navigate(`/orders/${orderId}`);
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
      REFUNDED: 'default'
    };
    return statusColors[status] || 'default';
  };

  const getStatusText = (status) => {
    const statusTexts = {
      PENDING: '待付款',
      PAID: '已付款',
      PROCESSING: '處理中',
      SHIPPED: '已出貨',
      DELIVERED: '已送達',
      CANCELLED: '已取消',
      REFUNDED: '已退款'
    };
    return statusTexts[status] || status;
  };

  const canCancelOrder = (status) => {
    return ['PENDING', 'PAID'].includes(status);
  };

  const handleFilterClick = (event) => {
    setFilterAnchorEl(event.currentTarget);
  };

  const handleFilterClose = () => {
    setFilterAnchorEl(null);
  };

  const handleFilterChange = (status) => {
    setStatusFilter(status);
    setPage(1);
    handleFilterClose();
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
            onClick={() => navigate('/dashboard')}
            sx={{ mr: 2 }}
          >
            返回
          </Button>
          <Typography variant="h4" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
            <OrderIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            我的訂單
          </Typography>
          <Button
            variant="outlined"
            startIcon={<FilterIcon />}
            onClick={handleFilterClick}
          >
            篩選: {statusFilter === 'ALL' ? '全部' : getStatusText(statusFilter)}
          </Button>
          <Menu
            anchorEl={filterAnchorEl}
            open={Boolean(filterAnchorEl)}
            onClose={handleFilterClose}
          >
            <MenuItem onClick={() => handleFilterChange('ALL')}>全部訂單</MenuItem>
            <MenuItem onClick={() => handleFilterChange('PENDING')}>待付款</MenuItem>
            <MenuItem onClick={() => handleFilterChange('PAID')}>已付款</MenuItem>
            <MenuItem onClick={() => handleFilterChange('PROCESSING')}>處理中</MenuItem>
            <MenuItem onClick={() => handleFilterChange('SHIPPED')}>已出貨</MenuItem>
            <MenuItem onClick={() => handleFilterChange('DELIVERED')}>已送達</MenuItem>
            <MenuItem onClick={() => handleFilterChange('CANCELLED')}>已取消</MenuItem>
          </Menu>
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
        ) : orders.length === 0 ? (
          // 空訂單列表
          <Paper sx={{ p: 6, textAlign: 'center' }}>
            <ReceiptIcon sx={{ fontSize: 80, color: 'text.secondary', mb: 2 }} />
            <Typography variant="h5" gutterBottom color="text.secondary">
              暫無訂單
            </Typography>
            <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
              您還沒有任何訂單記錄
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
              開始購物
            </Button>
          </Paper>
        ) : (
          // 訂單列表
          <>
            {/* 前端分页显示 */}
            {orders.slice((page - 1) * 10, page * 10).map((order) => (
              <Card key={order.id} sx={{ mb: 3, boxShadow: 2 }}>
                <CardContent>
                  {/* 訂單頭部 */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 2 }}>
                    <Box>
                      <Typography variant="h6" sx={{ fontWeight: 'bold' }}>
                        訂單編號: {order.orderNumber}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        下單時間: {formatDate(order.createdAt)}
                      </Typography>
                    </Box>
                    <Chip
                      label={getStatusText(order.status)}
                      color={getStatusColor(order.status)}
                      sx={{ fontWeight: 'bold' }}
                    />
                  </Box>

                  <Divider sx={{ my: 2 }} />

                  {/* 訂單商品摘要 */}
                  <Box sx={{ mb: 2 }}>
                    <Typography variant="body2" color="text.secondary" gutterBottom>
                      共 {order.orderItems?.length || 0} 件商品
                    </Typography>
                    <Grid container spacing={1}>
                      {order.orderItems?.slice(0, 3).map((item, index) => (
                        <Grid item key={index}>
                          <Chip
                            size="small"
                            label={`${item.productName} x${item.quantity}`}
                            variant="outlined"
                          />
                        </Grid>
                      ))}
                      {order.orderItems?.length > 3 && (
                        <Grid item>
                          <Chip
                            size="small"
                            label={`+${order.orderItems.length - 3} 更多`}
                            variant="outlined"
                          />
                        </Grid>
                      )}
                    </Grid>
                  </Box>

                  {/* 收貨信息 */}
                  {order.shippingAddress && (
                    <Box sx={{ mb: 2 }}>
                      <Typography variant="body2" color="text.secondary">
                        收貨人: {order.receiverName} {order.receiverPhone}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        收貨地址: {order.receiverAddress}
                      </Typography>
                    </Box>
                  )}

                  <Divider sx={{ my: 2 }} />

                  {/* 訂單底部 */}
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <Box>
                      <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
                        總金額: {formatPrice(order.totalAmount)}
                      </Typography>
                    </Box>
                    <Stack direction="row" spacing={1}>
                      <Button
                        variant="outlined"
                        startIcon={<ViewIcon />}
                        onClick={() => handleViewOrder(order.id)}
                      >
                        查看詳情
                      </Button>
                      {canCancelOrder(order.status) && (
                        <Button
                          variant="outlined"
                          color="error"
                          startIcon={<CancelIcon />}
                          onClick={() => {
                            setSelectedOrderId(order.id);
                            setCancelDialogOpen(true);
                          }}
                        >
                          取消訂單
                        </Button>
                      )}
                    </Stack>
                  </Box>
                </CardContent>
              </Card>
            ))}

            {/* 分頁 */}
            {totalPages > 1 && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
                <Pagination
                  count={totalPages}
                  page={page}
                  onChange={(e, value) => setPage(value)}
                  color="primary"
                  size="large"
                />
              </Box>
            )}
          </>
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

export default OrderList;

