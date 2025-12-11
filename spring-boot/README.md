# Demo Application

一個基於 Spring Boot 的完整後端應用程式，提供用戶認證、角色管理和雙因素驗證功能。

## 📋 目錄

- [功能特性](#功能特性)
- [技術棧](#技術棧)
- [快速開始](#快速開始)
- [API 文檔](#api-文檔)
- [安全機制](#安全機制)
- [項目結構](#項目結構)
- [配置說明](#配置說明)
- [部署指南](#部署指南)
- [開發指南](#開發指南)

## 🚀 功能特性

### 核心功能
- ✅ **用戶註冊與登入**：支援前台和後台用戶
- ✅ **JWT 認證**：無狀態的令牌認證機制
- ✅ **雙因素驗證 (2FA)**：Google Authenticator TOTP 支援
- ✅ **忘記密碼**：基於 TOTP 的安全密碼重置
- ✅ **角色管理**：多角色權限控制
- ✅ **全域異常處理**：統一錯誤回應格式

### 安全特性
- 🔐 **密碼加密**：BCrypt 雜湊演算法
- 🔐 **JWT 令牌**：安全的會話管理
- 🔐 **TOTP 雙因素認證**：增強帳戶安全性
- 🔐 **軟刪除**：保護數據完整性
- 🔐 **帳戶狀態管理**：啟用/停用、鎖定等狀態控制

### 數據管理
- 📊 **JPA/Hibernate**：物件關係映射
- 📊 **MySQL 支援**：關聯式數據庫
- 📊 **數據驗證**：Bean Validation
- 📊 **審計功能**：創建/更新時間追蹤

## 🛠 技術棧

- **後端框架**：Spring Boot 3.5.6
- **認證授權**：Spring Security + JWT
- **數據庫**：MySQL 8
- **ORM**：JPA/Hibernate
- **建構工具**：Maven
- **容器化**：Docker + Docker Compose
- **Java 版本**：17
- **雙因素驗證**：Google Authenticator (TOTP)

## 🏃‍♂️ 快速開始

### 前提條件
- Docker 和 Docker Compose
- Java 17 (開發環境)
- Maven (開發環境)

### 使用 Docker 啟動

1. **克隆專案**
```bash
git clone <repository-url>
cd demo
```

2. **啟動服務**
```bash
docker-compose up -d --build
```

3. **驗證服務**
```bash
# 檢查服務狀態
docker-compose ps

# 查看應用日誌
docker-compose logs app
```

應用程式將在 `http://localhost:8080` 啟動

### 本地開發環境

1. **配置數據庫**
```bash
# 啟動 MySQL
docker-compose up -d mysql
```

2. **運行應用**
```bash
./mvnw spring-boot:run
```

## 📡 API 文檔

### 前台用戶認證 API (`/api/v1/auth`)

#### 用戶註冊
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "用戶名稱",
  "email": "user@example.com",
  "password": "password123"
}
```

#### 前台用戶登入
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

#### 檢查用戶登入狀態
```http
GET /api/v1/auth/check-login/{email}
```

#### 設定 TOTP (雙因素驗證)
```http
POST /api/v1/auth/setup-totp
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### 驗證並啟用 TOTP
```http
POST /api/v1/auth/verify-enable-totp
Content-Type: application/json

{
  "email": "user@example.com",
  "secret": "TOTP_SECRET",
  "code": 123456
}
```

#### 忘記密碼 (需要 TOTP)
```http
POST /api/v1/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com",
  "newPassword": "newPassword123",
  "totpCode": 123456
}
```

#### 停用 TOTP
```http
POST /api/v1/auth/disable-totp?email=user@example.com
```

### 前台用戶功能 API (`/api/v1/user`)
*需要 USER 或 ADMIN 角色*

#### 獲取當前用戶資訊
```http
GET /api/v1/user/me
Authorization: Bearer {jwt_token}
```

#### 獲取指定用戶資訊
```http
GET /api/v1/user/{id}
Authorization: Bearer {jwt_token}
```

#### 更新個人資訊
```http
PUT /api/v1/user/profile
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "新名稱",
  "email": "newemail@example.com"
}
```

#### 獲取用戶統計信息
```http
GET /api/v1/user/stats
Authorization: Bearer {jwt_token}
```

### 後台管理 API (`/admin/v1`)
*需要 ADMIN 角色*

#### 後台管理員登入
```http
POST /admin/v1/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```

#### 創建管理員帳號
```http
POST /admin/v1/create-admin
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "管理員名稱",
  "email": "admin@example.com",
  "password": "password123"
}
```

#### 創建前台用戶帳號
```http
POST /admin/v1/create-user
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "用戶名稱",
  "email": "user@example.com",
  "password": "password123"
}
```

#### 獲取所有用戶列表
```http
GET /admin/v1/users
Authorization: Bearer {jwt_token}
```

#### 根據角色獲取用戶列表
```http
GET /admin/v1/users/role/{role}
Authorization: Bearer {jwt_token}
```
可用角色: `USER`, `ADMIN`

#### 獲取所有管理員
```http
GET /admin/v1/admins
Authorization: Bearer {jwt_token}
```

#### 獲取所有前台用戶
```http
GET /admin/v1/frontend-users
Authorization: Bearer {jwt_token}
```

#### 停用/啟用用戶
```http
PUT /admin/v1/users/{userId}/toggle-status
Authorization: Bearer {jwt_token}
```

#### 軟刪除用戶
```http
DELETE /admin/v1/users/{userId}
Authorization: Bearer {jwt_token}
```

#### 恢復已刪除的用戶
```http
PUT /admin/v1/users/{userId}/restore
Authorization: Bearer {jwt_token}
```

#### 為用戶添加角色
```http
PUT /admin/v1/users/{userId}/add-role/{role}
Authorization: Bearer {jwt_token}
```

#### 移除用戶角色
```http
PUT /admin/v1/users/{userId}/remove-role/{role}
Authorization: Bearer {jwt_token}
```

#### 獲取已刪除的用戶列表
```http
GET /admin/v1/deleted-users
Authorization: Bearer {jwt_token}
```

#### 獲取用戶統計信息
```http
GET /admin/v1/statistics
Authorization: Bearer {jwt_token}
```

### 回應格式

所有 API 都遵循統一的回應格式：

```json
{
  "success": true,
  "message": "操作成功",
  "data": {
    // 回應數據
  }
}
```

錯誤回應：
```json
{
  "success": false,
  "message": "錯誤訊息",
  "data": null
}
```

## 🔒 安全機制

### JWT 認證
- 使用 RS256 演算法
- 令牌包含用戶身份和角色信息
- 支援令牌過期檢查

### TOTP 雙因素驗證
- 基於時間的一次性密碼
- 與 Google Authenticator 相容
- 30 秒時間窗口
- SHA1 演算法，6 位數字

### 密碼安全
- BCrypt 雜湊演算法
- 鹽值隨機生成
- 密碼複雜度驗證

### 全域異常處理
- 統一錯誤回應格式
- 詳細的錯誤分類
- 安全的錯誤訊息過濾

## 📁 項目結構

```
src/main/java/com/example/demo/
├── DemoApplication.java          # 應用程式入口
├── config/
│   └── SecurityConfig.java       # Spring Security 配置
├── controller/
│   ├── AuthController.java       # 認證控制器
│   ├── backend/
│   │   └── AdminController.java  # 後台管理控制器
│   └── frontend/
│       └── UserController.java   # 前台用戶控制器
├── dto/
│   ├── request/                   # 請求 DTO
│   │   ├── ForgotPasswordRequest.java
│   │   ├── LoginRequest.java
│   │   ├── SetupTotpRequest.java
│   │   ├── VerifyAndEnableTotpRequest.java
│   │   ├── VerifyTotpRequest.java
│   │   ├── backend/               # 後台專用請求 DTO
│   │   └── frontend/              # 前台專用請求 DTO
│   └── response/                  # 回應 DTO
│       ├── ApiResponse.java       # 統一回應格式
│       ├── LoginResponse.java     # 登入回應
│       ├── TestResponse.java      # 測試回應
│       ├── TotpSetupResponse.java # TOTP 設定回應
│       └── UserResponse.java      # 用戶資訊回應
├── entity/
│   └── User.java                  # 用戶實體
├── enums/
│   └── UserRole.java             # 用戶角色枚舉
├── exception/
│   ├── AccountStatusException.java # 帳戶狀態異常
│   ├── CustomException.java      # 自訂異常基類
│   ├── GlobalExceptionHandler.java # 全域異常處理器
│   └── UserNotFoundException.java # 用戶不存在異常
├── filter/
│   └── JwtAuthenticationFilter.java # JWT 認證過濾器
├── repository/
│   └── UserRepository.java       # 用戶數據存取層
├── seed/
│   └── DataSeeder.java            # 數據播種器
├── service/
│   ├── AuthService.java          # 認證服務接口
│   ├── CustomUserDetailsService.java # Spring Security 用戶詳情服務
│   ├── TotpService.java          # TOTP 服務接口
│   ├── backend/                   # 後台服務
│   ├── frontend/                  # 前台服務
│   └── impl/                      # 服務實現類
└── util/
    └── JwtUtil.java               # JWT 工具類
```

### 配置檔案結構
```
src/main/resources/
├── application.properties         # 主要配置檔案
├── application-prod.properties    # 生產環境配置
├── static/                        # 靜態資源
└── templates/                     # 模板檔案
```

### Docker 相關檔案
```
project-root/
├── docker-compose.yml            # 主要 Docker Compose 配置
├── docker-compose-postgres.yml   # PostgreSQL 版本配置
├── docker-compose2.yml           # 替代配置
├── Dockerfile                     # Docker 鏡像構建檔案
└── README-Docker.md              # Docker 使用說明
```

## 🚀 部署指南

### Docker 部署 (推薦)
**啟動生產環境**
```bash
docker-compose -f docker-compose.yml up -d --build
```

### 傳統部署

1. **建構應用**
```bash
./mvnw clean package -DskipTests
```

2. **運行 JAR**
```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

## 👨‍💻 開發指南

### 本地開發設置

1. **啟動數據庫**
```bash
docker-compose up -d mysql
```

2. **運行應用**
```bash
./mvnw spring-boot:run
```

3. **熱重載**
```bash
# 使用 Spring Boot DevTools 自動重載
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

### 代碼規範

- 使用 Lombok 減少樣板代碼
- 遵循 RESTful API 設計原則
- 使用 Builder 模式構建複雜對象
- 統一異常處理和回應格式

### 測試
```bash
# 運行所有測試
./mvnw test

# 運行特定測試
./mvnw test -Dtest=DemoApplicationTests
```

### 日誌查看
```bash
# 查看應用日誌
docker-compose logs -f app

# 查看 MySQL 日誌
docker-compose logs -f mysql
```

## 📄 許可證

此專案僅供學習和開發使用。


---

**開發者**: [你的名字]  
**最後更新**: 2025年10月
