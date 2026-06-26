# 图书管理系统后端重构设计规范

## 1. 项目概述

### 1.1 背景

当前图书管理系统后端基于 Node.js + Express 实现，存在以下问题：
- 无 Service 层，Controller 直接操作数据库
- CORS 未限制，存在安全隐患
- 无日志框架，使用 console.log 调试
- 无 API 文档
- 无参数校验
- 无全局异常处理

### 1.2 目标

将后端从 Node.js 完全重写为 Java Spring Boot + JDBC，同时补全企业级特性。

### 1.3 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.x |
| Java 版本 | OpenJDK | 17 LTS |
| 构建工具 | Maven | 3.9+ |
| 数据库 | MySQL | 8.x |
| 数据库连接 | JDBC (Spring Data JDBC) | - |
| API 文档 | Springdoc OpenAPI | 2.x |
| 日志 | SLF4J + Logback | - |
| 参数校验 | Jakarta Bean Validation | 3.x |
| JWT | jjwt | 0.12.x |

## 2. 系统架构

### 2.1 架构模式

采用经典三层架构 + 过滤器模式：

```
┌─────────────────────────────────────────────────────────┐
│                      Client (Vue 3)                     │
└─────────────────────────────────────────────────────────┘
                            │ HTTP/JSON
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   JwtAuthFilter                         │
│              (JWT 认证过滤器)                            │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│              GlobalExceptionHandler                     │
│           (全局异常处理)                                  │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   Controller 层                         │
│           (请求处理、参数校验、响应封装)                    │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                   Service 层                            │
│           (业务逻辑、事务管理)                            │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                     DAO 层                              │
│           (JDBC Template 数据访问)                       │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│                    MySQL 数据库                          │
└─────────────────────────────────────────────────────────┘
```

### 2.2 项目结构

```
library-backend/
├── src/
│   ├── main/
│   │   ├── java/com/library/
│   │   │   ├── LibraryApplication.java          # 启动类
│   │   │   ├── config/
│   │   │   │   ├── WebConfig.java               # CORS 配置
│   │   │   │   └── JacksonConfig.java           # JSON 序列化配置
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java          # 认证控制器
│   │   │   │   ├── BookController.java          # 书籍控制器
│   │   │   │   ├── BorrowController.java        # 借阅控制器
│   │   │   │   └── UserController.java          # 用户控制器
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java             # 认证服务
│   │   │   │   ├── BookService.java             # 书籍服务
│   │   │   │   ├── BorrowService.java           # 借阅服务
│   │   │   │   └── UserService.java             # 用户服务
│   │   │   ├── dao/
│   │   │   │   ├── UserDao.java                 # 用户数据访问
│   │   │   │   ├── BookDao.java                 # 书籍数据访问
│   │   │   │   └── BorrowRecordDao.java         # 借阅记录数据访问
│   │   │   ├── model/
│   │   │   │   ├── User.java                    # 用户实体
│   │   │   │   ├── Book.java                    # 书籍实体
│   │   │   │   └── BorrowRecord.java            # 借阅记录实体
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java        # 登录请求
│   │   │   │   │   ├── RegisterRequest.java     # 注册请求
│   │   │   │   │   └── BookRequest.java         # 书籍请求
│   │   │   │   └── response/
│   │   │   │       ├── ApiResponse.java         # 统一响应格式
│   │   │   │       ├── LoginResponse.java       # 登录响应
│   │   │   │       └── PageResponse.java        # 分页响应
│   │   │   ├── filter/
│   │   │   │   └── JwtAuthFilter.java           # JWT 认证过滤器
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   │   │   ├── BusinessException.java       # 业务异常
│   │   │   │   └── ErrorCode.java               # 错误码枚举
│   │   │   └── util/
│   │   │       └── JwtUtil.java                 # JWT 工具类
│   │   └── resources/
│   │       ├── application.yml                  # 主配置文件
│   │       ├── application-dev.yml              # 开发环境配置
│   │       └── logback-spring.xml               # 日志配置
│   └── test/
│       └── java/com/library/
│           └── service/
│               ├── AuthServiceTest.java
│               ├── BookServiceTest.java
│               └── BorrowServiceTest.java
├── docs/
│   └── api/                                     # API 文档（自动生成）
├── sql/
│   ├── schema.sql                               # 建表语句
│   └── data.sql                                 # 初始数据
├── pom.xml                                      # Maven 配置
└── README.md
```

## 3. 数据库设计

### 3.1 表结构（沿用现有设计）

#### users 表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | INT | 用户ID | 主键，自增 |
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| password | VARCHAR(255) | 密码（BCrypt加密） | 非空 |
| name | VARCHAR(50) | 姓名 | 非空 |
| email | VARCHAR(100) | 邮箱 | |
| role | ENUM('user','admin') | 角色 | 默认 'user' |
| status | TINYINT | 状态 | 1正常/0禁用，默认1 |
| created_at | DATETIME | 创建时间 | 默认 CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | 自动更新 |

#### books 表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | INT | 书籍ID | 主键，自增 |
| isbn | VARCHAR(20) | ISBN号 | 唯一 |
| title | VARCHAR(200) | 书名 | 非空 |
| author | VARCHAR(100) | 作者 | 非空 |
| category | VARCHAR(50) | 分类 | |
| location | VARCHAR(50) | 位置（如A-01-01） | |
| total_quantity | INT | 总库存 | 默认0 |
| available_quantity | INT | 可借数量 | 默认0 |
| description | TEXT | 简介 | |
| created_at | DATETIME | 创建时间 | 默认 CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | 自动更新 |

#### borrow_records 表

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| id | INT | 记录ID | 主键，自增 |
| user_id | INT | 用户ID | 外键，级联删除 |
| book_id | INT | 书籍ID | 外键，级联删除 |
| borrow_date | DATETIME | 借阅日期 | 默认 CURRENT_TIMESTAMP |
| due_date | DATETIME | 应还日期 | 非空 |
| return_date | DATETIME | 实际归还日期 | 空表示未归还 |
| status | ENUM('borrowing','returned') | 状态 | 默认 'borrowing' |
| created_at | DATETIME | 创建时间 | 默认 CURRENT_TIMESTAMP |
| updated_at | DATETIME | 更新时间 | 自动更新 |

### 3.2 索引设计

```sql
-- borrow_records 索引
CREATE INDEX idx_borrow_user_id ON borrow_records(user_id);
CREATE INDEX idx_borrow_book_id ON borrow_records(book_id);
CREATE INDEX idx_borrow_status ON borrow_records(status);

-- books 索引
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_books_category ON books(category);

-- users 索引
CREATE UNIQUE INDEX idx_users_username ON users(username);
```

## 4. API 设计

### 4.1 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

#### 分页响应

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

### 4.2 认证接口

#### POST /api/auth/login

**请求体：**
```json
{
  "username": "admin",
  "password": "password"
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "id": 1,
      "username": "admin",
      "name": "管理员",
      "email": "admin@library.com",
      "role": "admin"
    }
  }
}
```

**失败响应：**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null
}
```

#### POST /api/auth/register

**请求体：**
```json
{
  "username": "newuser",
  "password": "password123",
  "name": "新用户",
  "email": "new@example.com"
}
```

**参数校验规则：**
- `username`: 非空，3-50 字符
- `password`: 非空，6-50 字符
- `name`: 非空，2-50 字符
- `email`: 可选，邮箱格式

### 4.3 书籍接口

#### GET /api/books

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| search | String | 否 | 搜索关键词（标题/作者/ISBN） |
| category | String | 否 | 分类筛选 |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

**响应：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "isbn": "9787111111111",
        "title": "JavaScript高级程序设计",
        "author": "Nicholas C. Zakas",
        "category": "编程",
        "location": "A-01-01",
        "totalQuantity": 5,
        "availableQuantity": 5,
        "description": "经典的JavaScript学习书籍"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 10
  }
}
```

#### POST /api/books（管理员）

**请求体：**
```json
{
  "isbn": "9787111111111",
  "title": "JavaScript高级程序设计",
  "author": "Nicholas C. Zakas",
  "category": "编程",
  "location": "A-01-01",
  "totalQuantity": 5,
  "description": "经典的JavaScript学习书籍"
}
```

**参数校验规则：**
- `title`: 非空，1-200 字符
- `author`: 非空，1-100 字符
- `isbn`: 可选，唯一
- `totalQuantity`: 非空，>= 0

### 4.4 借阅接口

#### POST /api/borrow/{bookId}

**路径参数：**
- `bookId`: 书籍ID

**业务规则：**
1. 检查书籍是否存在
2. 检查库存是否充足
3. 检查用户是否已借阅该书（未归还）
4. 创建借阅记录，借期30天
5. 扣减库存

**事务保证：** 使用 `@Transactional` 注解确保借书操作的原子性。

#### POST /api/borrow/return/{recordId}

**业务规则：**
1. 检查借阅记录是否存在
2. 检查是否已归还
3. 检查是否有权归还（本人或管理员）
4. 更新借阅记录状态
5. 增加库存

### 4.5 用户接口

#### GET /api/users（管理员）

**查询参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| search | String | 否 | 搜索关键词（用户名/姓名/邮箱） |
| page | Integer | 否 | 页码，默认1 |
| pageSize | Integer | 否 | 每页数量，默认10 |

#### PUT /api/users/{id}

**权限规则：**
- 普通用户只能修改自己的 name 和 email
- 管理员可以修改任何用户的 name、email、role、status

## 5. 安全设计

### 5.1 CORS 配置

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

### 5.2 JWT 认证

**Token 格式：**
```
Authorization: Bearer <token>
```

**Token 内容：**
```json
{
  "userId": 1,
  "username": "admin",
  "role": "admin",
  "exp": 1719446400
}
```

**过期时间：** 7天

### 5.3 密码加密

使用 BCrypt 算法，salt rounds = 10。

### 5.4 全局异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        log.error("系统异常", e);
        return ApiResponse.error(500, "服务器内部错误");
    }
}
```

## 6. 企业级特性

### 6.1 日志配置

使用 SLF4J + Logback，配置文件 `logback-spring.xml`：

- **控制台输出**：INFO 级别，彩色格式
- **文件输出**：DEBUG 级别，按日期滚动
- **日志格式**：`%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

### 6.2 API 文档

使用 Springdoc OpenAPI，启动后访问：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### 6.3 输入参数校验

使用 Jakarta Bean Validation：

```java
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50之间")
    private String password;

    @NotBlank(message = "姓名不能为空")
    @Size(min = 2, max = 50, message = "姓名长度必须在2-50之间")
    private String name;

    @Email(message = "邮箱格式不正确")
    private String email;
}
```

### 6.4 事务管理

```java
@Service
@Transactional
public class BorrowService {
    public void borrowBook(Long userId, Long bookId) {
        // 1. 检查书籍
        Book book = bookDao.findById(bookId);
        if (book == null) throw new BusinessException(404, "书籍不存在");
        if (book.getAvailableQuantity() <= 0) throw new BusinessException(400, "库存不足");

        // 2. 检查是否已借阅
        if (borrowRecordDao.existsActiveBorrow(userId, bookId)) {
            throw new BusinessException(400, "已借阅该书");
        }

        // 3. 创建借阅记录
        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(30));
        record.setStatus("borrowing");
        borrowRecordDao.save(record);

        // 4. 扣减库存
        bookDao.decreaseAvailable(bookId);
    }
}
```

## 7. 配置管理

### application.yml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
    username: root
    password: ${DB_PASSWORD:123456}
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000

jwt:
  secret: ${JWT_SECRET:your-secret-key-here-change-in-production}
  expiration: 604800000  # 7天（毫秒）

logging:
  level:
    com.library: DEBUG
    org.springframework: INFO
```

## 8. 实现计划

### 阶段一：项目基础搭建（Day 1）
1. 创建 Spring Boot 项目骨架
2. 配置 Maven 依赖
3. 配置数据源和日志
4. 创建基础包结构

### 阶段二：核心功能实现（Day 2-3）
1. 实现 User/Book/BorrowRecord 实体和 DAO
2. 实现 Service 层业务逻辑
3. 实现 Controller 层 API
4. 实现 JWT 认证过滤器

### 阶段三：企业级特性（Day 4）
1. 实现全局异常处理
2. 配置参数校验
3. 配置 API 文档
4. 编写日志配置

### 阶段四：前端对接（Day 5）
1. 调整前端 API baseURL 为 `/api`
2. 测试所有功能
3. 修复问题

## 9. 待确认事项

- [ ] MySQL 数据库密码：沿用现有配置还是重新设置？
- [ ] JWT Secret：是否需要生成新的密钥？
- [ ] 前端 API baseURL：是否需要修改为 `http://localhost:8080/api`？

---

**文档版本**: v1.0
**创建日期**: 2026-06-26
**状态**: 待审核
