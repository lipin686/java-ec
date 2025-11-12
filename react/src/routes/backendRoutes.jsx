import React from 'react';
import { Routes, Route } from 'react-router-dom';
import AdminProtectedRoute from '../components/common/AdminProtectedRoute';
import AdminLayout from '../components/layout/AdminLayout';
import AdminLogin from '../pages/backend/Login/AdminLogin';
import AdminDashboard from '../pages/backend/Dashboard/AdminDashboard';
import CreateUserForm from '../pages/backend/CreateUser/CreateUserForm';
import ProductList from '../pages/backend/Products/ProductList';
import ProductForm from '../pages/backend/Products/ProductForm';
import ProductDetail from '../pages/backend/Products/ProductDetail';
import UserList from '../pages/backend/User/UserList';
import AdminList from '../pages/backend/Admin/AdminList';

const BackendRoutes = () => (
  <Routes>
    {/* 後台登入頁不需要 layout */}
    <Route path="/admin/login" element={<AdminLogin />} />
    {/* 後台路由統一用 AdminLayout 包裹 */}
    <Route path="/admin" element={<AdminProtectedRoute><AdminLayout /></AdminProtectedRoute>}>
      <Route path="dashboard" element={<AdminDashboard />} />
      <Route path="users" element={<UserList />} />
      <Route path="admins" element={<AdminList />} />
      <Route path="create-user" element={<CreateUserForm />} />
      <Route path="products" element={<ProductList />} />
      <Route path="products/create" element={<ProductForm />} />
      <Route path="products/:id/edit" element={<ProductForm />} />
      <Route path="products/:id" element={<ProductDetail />} />
      {/* 你可以在這裡擴充更多 admin 子路由 */}
    </Route>
  </Routes>
);

export default BackendRoutes;
