## 快速部署

### 使用 Docker Compose 一鍵啟動

```bash
# 在專案根目錄執行
docker-compose up --build -d
```
### 測試
docker exec demo-app ./mvnw test

### 服務訪問地址

- **前端應用**: http://localhost:3000
- **後端 API**: http://localhost:8080
- **資料庫**: localhost:3306

## 專案功能

### 用戶功能
- ✅ 用戶註冊與登入
- ✅ JWT Token 身份驗證
- ✅ 個人儀表板
- ✅ 忘記密碼功能
- ✅ 自動登出（Token 過期）

### 管理員功能
- ✅ 管理員專用登入
- ✅ 用戶權限管理
- ✅ 用戶狀態控制（啟用/停用）
- ✅ 創建新用戶/管理員
- ✅ 用戶列表查詢與搜尋
- ✅ 系統統計與監控

### 系統特色
- 🔐 角色權限分離（USER/ADMIN）
- 🛡️ 完整的表單驗證
- 🚀 前後端分離架構
- 🐳 Docker 容器化部署

## 技術棧

- **前端**: React + Vite + Ant Design
- **後端**: Spring Boot + Spring Security
- **資料庫**: MySQL
- **認證**: JWT Token
- **部署**: Docker + Docker Compose

---
