# Docker环境使用说明

## 启动应用

使用以下命令启动整个环境（包括Spring Boot应用和MySQL数据库）：

```bash
docker-compose up -d --build
```

## 测试应用

启动完成后，你可以测试以下端点：

1. **Hello World**: http://localhost:8080/
2. **带MySQL信息的Hello**: http://localhost:8080/hello
3. **测试数据库连接**: http://localhost:8080/test-db
4. **获取所有用户**: http://localhost:8080/users
5. **创建用户** (POST): http://localhost:8080/users

## 创建用户示例

使用curl或Postman发送POST请求：

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com"}'
```

## 查看日志

```bash
# 查看应用日志
docker-compose logs app

# 查看MySQL日志
docker-compose logs mysql

# 实时查看所有日志
docker-compose logs -f
```

## 停止环境

```bash
docker-compose down
```

## 完全清理（包括数据）

```bash
docker-compose down -v
```

## 数据库配置

- **数据库名**: demo_db
- **用户名**: demo_user  
- **密码**: demo_password
- **Root密码**: rootpassword
- **端口**: 3306

数据库数据会持久化保存在Docker volume中。
