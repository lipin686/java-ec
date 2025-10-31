import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import AdminProtectedRoute from './components/AdminProtectedRoute';

// 前台頁面 - 使用Material-UI套件
import Login from './pages/User/Login/Login';
import Register from './pages/User/Register/Register';
import Dashboard from './pages/User/Dashboard/Dashboard';

// 後台頁面 - 使用Ant Design套件
import AdminLogin from './pages/Admin/Login/AdminLogin';
import AdminDashboard from './pages/Admin/Dashboard/AdminDashboard';
import CreateUserForm from './pages/Admin/CreateUser/CreateUserForm';

import './App.css';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Routes>
            {/* 首頁重定向到登入頁 */}
            <Route path="/" element={<Navigate to="/login" replace />} />

            {/* 前台路由 */}
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

            {/* 後台管理路由 */}
            <Route path="/admin/login" element={<AdminLogin />} />
            <Route
              path="/admin/dashboard"
              element={
                <AdminProtectedRoute>
                  <AdminDashboard />
                </AdminProtectedRoute>
              }
            />
            <Route
              path="/admin/create-user"
              element={
                <AdminProtectedRoute>
                  <CreateUserForm />
                </AdminProtectedRoute>
              }
            />

            {/* 404頁面 */}
            <Route path="*" element={<Navigate to="/login" replace />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
