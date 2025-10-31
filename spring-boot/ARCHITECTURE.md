# Spring Boot 后端架构说明

## 项目结构
```
src/main/java/com/example/demo/
├── controller/          # Controller层 - 处理HTTP请求
│   └── UserController.java
├── service/            # Service层 - 业务逻辑
│   ├── UserService.java        # 接口
│   └── impl/
│       └── UserServiceImpl.java # 实现类
├── repository/         # Repository层 - 数据访问
│   └── UserRepository.java
├── entity/            # Entity层 - 实体类/数据模型
│   └── User.java
├── dto/               # DTO层 - 数据传输对象
│   ├── request/       # 请求DTO
│   │   └── UserCreateRequest.java
│   └── response/      # 响应DTO
│       ├── UserResponse.java
│       └── ApiResponse.java
└── DemoApplication.java # 启动类
```

## 各层职责

### 1. Controller层
- 处理HTTP请求和响应
- 参数验证
- 调用Service层处理业务逻辑
- 返回统一格式的响应

### 2. Service层
- 处理核心业务逻辑
- 事务管理
- 调用Repository层进行数据操作
- 数据转换（Entity <-> DTO）

### 3. Repository层  
- 数据访问层
- 继承JpaRepository获得基本CRUD操作
- 自定义查询方法

### 4. Entity层
- 实体类，对应数据库表
- 使用JPA注解映射
- 使用Lombok简化代码

### 5. DTO层
- 数据传输对象
- Request DTO：接收前端请求数据
- Response DTO：返回给前端的数据格式
- ApiResponse：统一响应格式

## 使用方式
当您需要新增功能时，按以下步骤：
1. 创建对应的Entity（如果需要新表）
2. 创建Repository接口
3. 创建Request/Response DTO
4. 创建Service接口和实现类
5. 创建Controller

## 示例API
- POST /api/users - 创建用户
- GET /api/users/{id} - 根据ID获取用户

这个架构遵循了Spring Boot的最佳实践，分层清晰，易于维护和扩展。
