# 图书馆管理系统

基于 Vue 3 + Java 17 + Spring Boot 3.2 + MySQL 的图书馆管理系统。

## 功能特性

- 用户认证：登录、注册
- 书籍管理：添加、编辑、删除、搜索书籍
- 借阅管理：借书、还书、查看借阅记录
- 用户管理：管理员可管理用户
- 角色权限：普通用户和管理员权限分离

## 技术栈

### 前端
- Vue 3
- Vue Router 4
- Pinia
- Element Plus
- Axios
- Vite

### 后端
- Java 17
- Spring Boot 3.2
- JDBC
- MySQL
- JWT
- Swagger UI

## 项目结构

```
项目/
├── backend/          # 后端代码 (Spring Boot)
│   ├── pom.xml       # Maven 配置
│   ├── src/main/java/com/library/
│   │   ├── LibraryApplication.java
│   │   ├── config/       # 配置类
│   │   ├── controller/   # 控制器
│   │   ├── service/      # 服务层
│   │   ├── model/        # 实体类
│   │   ├── dto/          # 数据传输对象
│   │   ├── filter/       # 过滤器
│   │   ├── exception/    # 异常处理
│   │   └── util/         # 工具类
│   └── src/main/resources/
│       ├── application.yml
│       └── logback-spring.xml
├── frontend/         # 前端代码
│   ├── src/
│   │   ├── api/      # API 封装
│   │   ├── router/   # 路由
│   │   ├── store/    # 状态管理
│   │   ├── views/    # 页面组件
│   │   ├── App.vue
│   │   └── main.js
│   ├── index.html
│   ├── vite.config.js
│   └── package.json
├── database/         # 数据库
│   └── init.sql      # 初始化脚本
└── README.md
```

## 快速开始

### 1. 数据库配置

确保已安装 MySQL，然后执行初始化脚本：

**Windows (PowerShell):**
```powershell
# 方式1: 使用提供的批处理文件（推荐）
init-db.bat

# 方式2: 使用 Get-Content 管道
Get-Content database\init.sql | mysql -u root -p
```

**Windows (CMD) 或 Linux/Mac:**
```bash
mysql -u root -p < database/init.sql
```

或者在 MySQL 客户端中手动执行 `database/init.sql` 文件内容。

### 2. 后端配置

修改 `backend/src/main/resources/application.yml` 文件，配置数据库连接信息：

```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_management
    username: root
    password: 你的数据库密码
```

启动后端：

```bash
cd backend
mvn spring-boot:run
```

后端服务将在 http://localhost:8080 启动

API 文档（Swagger UI）：http://localhost:8080/swagger-ui.html

### 3. 前端配置

安装前端依赖并启动：

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 http://localhost:5173 启动

## 默认账号

系统初始化时会创建以下测试账号（密码均为：`password`）：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | password | 管理员 |
| user1 | password | 普通用户 |

> 注意：首次登录后，建议立即修改管理员密码！

## API 文档

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 书籍接口
- `GET /api/books` - 获取书籍列表
- `GET /api/books/:id` - 获取书籍详情
- `POST /api/books` - 添加书籍（管理员）
- `PUT /api/books/:id` - 更新书籍（管理员）
- `DELETE /api/books/:id` - 删除书籍（管理员）

### 借阅接口
- `GET /api/borrow/my` - 我的借阅记录
- `POST /api/borrow/:bookId` - 借书
- `POST /api/borrow/return/:recordId` - 还书
- `GET /api/borrow/all` - 所有借阅记录（管理员）

### 用户接口
- `GET /api/users` - 用户列表（管理员）
- `GET /api/users/:id` - 用户详情
- `PUT /api/users/:id` - 更新用户
- `DELETE /api/users/:id` - 删除用户（管理员）
