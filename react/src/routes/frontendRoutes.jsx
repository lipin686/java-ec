import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from '../components/common/ProtectedRoute';
import Login from '../pages/frontend/Login/Login';
import Register from '../pages/frontend/Register/Register';
import Dashboard from '../pages/frontend/Dashboard/Dashboard';
import Cart from '../pages/frontend/Cart/Cart';
import Checkout from '../pages/frontend/Order/Checkout';
import OrderList from '../pages/frontend/Order/OrderList';
import OrderDetail from '../pages/frontend/Order/OrderDetail';

const FrontendRoutes = () => (
  <Routes>
    <Route path="/" element={<Navigate to="/login" replace />} />
    <Route path="/login" element={<Login />} />
    <Route path="/register" element={<Register />} />
    <Route
      path="/dashboard"
      element={
        <ProtectedRoute>
          <Dashboard />
        </ProtectedRoute>
      }
    />
    <Route
      path="/cart"
      element={
        <ProtectedRoute>
          <Cart />
        </ProtectedRoute>
      }
    />
    <Route
      path="/checkout"
      element={
        <ProtectedRoute>
          <Checkout />
        </ProtectedRoute>
      }
    />
    <Route
      path="/orders"
      element={
        <ProtectedRoute>
          <OrderList />
        </ProtectedRoute>
      }
    />
    <Route
      path="/orders/:orderId"
      element={
        <ProtectedRoute>
          <OrderDetail />
        </ProtectedRoute>
      }
    />
    {/* 你可以在這裡擴充更多前台路由 */}
  </Routes>
);

export default FrontendRoutes;
