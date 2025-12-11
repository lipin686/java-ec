import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from '../components/common/ProtectedRoute';
import AdminProtectedRoute from '../components/common/AdminProtectedRoute';
import AdminLayout from '../components/layout/AdminLayout';

// 前台
import Home from '../pages/frontend/Home/Home';
import Login from '../pages/frontend/Login/Login';
import Register from '../pages/frontend/Register/Register';
import Dashboard from '../pages/frontend/Dashboard/Dashboard';
import FrontendProductDetail from '../pages/frontend/ProductDetail/ProductDetail';
import Cart from '../pages/frontend/Cart/Cart';
import Checkout from '../pages/frontend/Order/Checkout';
import OrderList from '../pages/frontend/Order/OrderList';
import OrderDetail from '../pages/frontend/Order/OrderDetail';

// 後台
import AdminLogin from '../pages/backend/Login/AdminLogin';
import AdminDashboard from '../pages/backend/Dashboard/AdminDashboard';
import CreateUserForm from '../pages/backend/CreateUser/CreateUserForm';
import ProductList from '../pages/backend/Products/ProductList';
import ProductForm from '../pages/backend/Products/ProductForm';
import ProductDetail from '../pages/backend/Products/ProductDetail';
import UserList from '../pages/backend/User/UserList';
import AdminList from '../pages/backend/Admin/AdminList';

const AppRoutes = () => (
  <Routes>
    {/* 前台公開頁面 - 不需要登入 */}
    <Route path="/" element={<Home />} />
    <Route path="/products/:id" element={<FrontendProductDetail />} />

    {/* 前台會員功能 */}
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

    {/* 後台 */}
    <Route path="/admin/login" element={<AdminLogin />} />
    <Route
      path="/admin"
      element={
        <AdminProtectedRoute>
          <AdminLayout />
        </AdminProtectedRoute>
      }
    >
      <Route path="dashboard" element={<AdminDashboard />} />
      <Route path="users" element={<UserList />} />
      <Route path="admins" element={<AdminList />} />
      <Route path="create-user" element={<CreateUserForm />} />
      <Route path="products" element={<ProductList />} />
      <Route path="products/create" element={<ProductForm />} />
      <Route path="products/:id/edit" element={<ProductForm />} />
      <Route path="products/:id" element={<ProductDetail />} />
    </Route>

    {/* 404 - 重定向到首頁 */}
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
);

export default AppRoutes;
