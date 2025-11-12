import React from 'react';
import {
  Box,
  Card,
  CardContent,
  Button,
  Typography,
  Container,
  Avatar,
  Grid,
  AppBar,
  Toolbar,
  Chip,
  Paper,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider
} from '@mui/material';
import {
  Person as PersonIcon,
  Logout as LogoutIcon,
  Email as EmailIcon,
  Badge as BadgeIcon,
  Edit as EditIcon,
  Settings as SettingsIcon,
  AdminPanelSettings as AdminIcon,
  Dashboard as DashboardIcon
} from '@mui/icons-material';
import { useAuth } from '../../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      {/* App Bar */}
      <AppBar
        position="static"
        sx={{
          background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
          boxShadow: '0 4px 12px rgba(0,0,0,0.15)'
        }}
      >
        <Toolbar>
          <Avatar sx={{ mr: 2, bgcolor: 'rgba(255,255,255,0.2)' }}>
            <PersonIcon />
          </Avatar>
          <Box sx={{ flexGrow: 1 }}>
            <Typography variant="h6" component="div">
              用戶儀表板
            </Typography>
            <Typography variant="body2" sx={{ opacity: 0.8 }}>
              歡迎回來，{user?.email}
            </Typography>
          </Box>
          <Button
            color="inherit"
            onClick={handleLogout}
            startIcon={<LogoutIcon />}
            sx={{
              bgcolor: 'rgba(255,255,255,0.1)',
              '&:hover': { bgcolor: 'rgba(255,255,255,0.2)' }
            }}
          >
            登出
          </Button>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
        <Grid container spacing={3}>
          {/* Welcome Card */}
          <Grid item xs={12}>
            <Card sx={{ boxShadow: 3, borderRadius: 2 }}>
              <CardContent sx={{ p: 4 }}>
                <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                  <Avatar
                    sx={{
                      width: 80,
                      height: 80,
                      mr: 3,
                      bgcolor: 'primary.main',
                      fontSize: '2rem'
                    }}
                  >
                    <PersonIcon fontSize="large" />
                  </Avatar>
                  <Box>
                    <Typography variant="h4" gutterBottom>
                      歡迎來到系統！
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                      您已成功登入前台系統
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* User Info Card */}
          <Grid item xs={12} md={8}>
            <Card sx={{ boxShadow: 2, borderRadius: 2, height: '100%' }}>
              <CardContent>
                <Typography variant="h5" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                  <BadgeIcon sx={{ mr: 1 }} />
                  用戶資訊
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <List>
                  <ListItem>
                    <ListItemIcon>
                      <EmailIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="郵箱"
                      secondary={user?.email}
                    />
                  </ListItem>

                  <ListItem>
                    <ListItemIcon>
                      <BadgeIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="用戶ID"
                      secondary={user?.id}
                    />
                  </ListItem>

                  <ListItem>
                    <ListItemIcon>
                      <PersonIcon color="primary" />
                    </ListItemIcon>
                    <ListItemText
                      primary="角色"
                      secondary={
                        <span>
                          {user?.roles?.map((role) => (
                            <Chip
                              key={role}
                              label={role}
                              color={role === 'ADMIN' ? 'error' : 'primary'}
                              size="small"
                              sx={{ mr: 1 }}
                              component="span"
                            />
                          ))}
                        </span>
                      }
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Grid>

          {/* Quick Actions Card */}
          <Grid item xs={12} md={4}>
            <Card sx={{ boxShadow: 2, borderRadius: 2, height: '100%' }}>
              <CardContent>
                <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center' }}>
                  <DashboardIcon sx={{ mr: 1 }} />
                  快速操作
                </Typography>
                <Divider sx={{ mb: 2 }} />

                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  <Button
                    variant="outlined"
                    startIcon={<EditIcon />}
                    fullWidth
                  >
                    編輯個人資料
                  </Button>

                  <Button
                    variant="outlined"
                    startIcon={<SettingsIcon />}
                    fullWidth
                  >
                    查看帳戶設定
                  </Button>

                  {user?.roles?.includes('ADMIN') && (
                    <Button
                      variant="contained"
                      startIcon={<AdminIcon />}
                      fullWidth
                      onClick={() => navigate('/admin/dashboard')}
                      sx={{
                        bgcolor: 'warning.main',
                        '&:hover': { bgcolor: 'warning.dark' }
                      }}
                    >
                      🔐 進入後台管理
                    </Button>
                  )}
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Status Card */}
          <Grid item xs={12}>
            <Paper
              sx={{
                p: 3,
                textAlign: 'center',
                background: 'linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%)',
                borderRadius: 2
              }}
            >
              <Typography variant="h6" color="primary" gutterBottom>
                系統狀態
              </Typography>
              <Typography variant="body2" color="text.secondary">
                所有系統運行正常 • 最後更新: {new Date().toLocaleString()}
              </Typography>
            </Paper>
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
};

export default Dashboard;
