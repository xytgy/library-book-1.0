# 图书管理系统后端 (Spring Boot)

基于 Spring Boot 3 + JDBC + MySQL 的图书馆管理系统后端。

## 技术栈

- Java 17
- Spring Boot 3.2.x
- Spring JDBC
- MySQL 8.x
- JWT (jjwt)
- Springdoc OpenAPI (Swagger UI)
- SLF4J + Logback

## 项目结构

```
library-backend/
├── src/main/java/com/library/
│   ├── LibraryApplication.java          # 启动类
│   ├── config/                          # 配置类
│   ├── controller/                      # 控制器层
│   ├── service/                         # 服务层
│   ├── model/                           # 实体类
│   ├── dto/                             # 数据传输对象
│   ├── filter/                          # JWT 过滤器
│   ├── exception/                       # 异常处理
│   └── util/                            # 工具类
├── src/main/resources/
│   ├── application.yml                  # 配置文件
│   └── logback-spring.xml               # 日志配置
└── sql/
    ├── schema.sql                       # 建表语句
    └── data.sql                         # 初始数据
```

## 快速开始

### 1. 数据库初始化

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/data.sql
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`，修改数据库密码：

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:你的数据库密码}
```

### 3. 启动项目

```bash
# 方式1：使用 Maven
mvn spring-boot:run

# 方式2：使用 IDEA 运行 LibraryApplication.main()
```

### 4. 访问 API 文档

启动后访问：http://localhost:8080/swagger-ui.html

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | password | 管理员 |
| user1 | password | 普通用户 |

## API 接口

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 书籍接口
- `GET /api/books` - 获取书籍列表
- `GET /api/books/{id}` - 获取书籍详情
- `POST /api/books` - 添加书籍（管理员）
- `PUT /api/books/{id}` - 更新书籍（管理员）
- `DELETE /api/books/{id}` - 删除书籍（管理员）

### 借阅接口
- `GET /api/borrow/my` - 我的借阅记录
- `POST /api/borrow/{bookId}` - 借书
- `POST /api/borrow/return/{recordId}` - 还书
- `GET /api/borrow/all` - 所有借阅记录（管理员）

### 用户接口
- `GET /api/users` - 用户列表（管理员）
- `GET /api/users/{id}` - 用户详情
- `PUT /api/users/{id}` - 更新用户
- `DELETE /api/users/{id}` - 删除用户（管理员）

## 环境变量

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_PASSWORD | 数据库密码 | 123456 |
| JWT_SECRET | JWT密钥 | your-secret-key-here... |
