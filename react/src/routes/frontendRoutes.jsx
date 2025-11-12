import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import ProtectedRoute from '../components/common/ProtectedRoute';
import Login from '../pages/frontend/Login/Login';
import Register from '../pages/frontend/Register/Register';
import Dashboard from '../pages/frontend/Dashboard/Dashboard';

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
    {/* 你可以在這裡擴充更多前台路由 */}
  </Routes>
);

export default FrontendRoutes;
