# 图书馆管理系统

基于 Java SE + JDBC + MySQL 的控制台图书馆管理系统。

## 功能特性

- 用户认证：登录、注册（BCrypt 密码加密）
- 书籍管理：添加、编辑、删除、搜索书籍（管理员）
- 借阅管理：借书、还书、查看借阅记录
- 用户管理：管理员可管理用户
- 角色权限：普通用户和管理员权限分离
- 彩色终端界面：使用 jansi 实现美化输出

## 技术栈

- Java 17（纯 Java SE，无框架）
- JDBC + HikariCP 连接池
- MySQL 数据库
- JWT 认证（jjwt）
- BCrypt 密码加密
- jansi 彩色终端输出
- Logback 日志

## 项目结构

```
library-book-1.0/
├── backend/                        # 后端代码（纯 Java SE）
│   ├── pom.xml                     # Maven 配置
│   ├── src/main/java/com/library/
│   │   ├── Main.java               # 程序入口
│   │   ├── model/                  # 实体类
│   │   │   ├── User.java
│   │   │   ├── Book.java
│   │   │   └── BorrowRecord.java
│   │   ├── dao/                    # 数据访问层（纯 JDBC）
│   │   │   ├── UserDao.java
│   │   │   ├── BookDao.java
│   │   │   └── BorrowRecordDao.java
│   │   ├── service/                # 业务逻辑层
│   │   │   ├── AuthService.java
│   │   │   ├── BookService.java
│   │   │   ├── BorrowService.java
│   │   │   └── UserService.java
│   │   ├── console/                # 控制台菜单
│   │   │   ├── ConsoleHelper.java
│   │   │   ├── AuthMenu.java
│   │   │   ├── BookMenu.java
│   │   │   ├── BorrowMenu.java
│   │   │   └── UserMenu.java
│   │   ├── db/                     # 数据库连接管理
│   │   │   └── DatabaseUtil.java
│   │   ├── exception/              # 异常处理
│   │   │   ├── BusinessException.java
│   │   │   └── ErrorCode.java
│   │   └── util/
│   │       └── JwtUtil.java        # JWT 工具
│   └── src/main/resources/
│       ├── application.properties  # 配置文件
│       └── logback.xml             # 日志配置
├── database/
│   └── init.sql                    # 数据库初始化脚本
└── README.md
```

## 快速开始

### 1. 初始化数据库

确保已安装 MySQL，然后执行初始化脚本：

```bash
mysql -u root -p123456 < database/init.sql
```

### 2. 修改数据库密码

编辑 `backend/src/main/resources/application.properties`：

```properties
db.url=jdbc:mysql://localhost:3306/library_management?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
db.user=root
db.password=你的数据库密码
```

### 3. 编译打包

```bash
cd backend
mvn clean package -DskipTests
```

### 4. 运行

```bash
java -jar target/library-backend-1.0.0.jar
```

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| user1 | 123456 | 普通用户 |

## 操作说明

### 未登录菜单
```
1. 用户登录
2. 用户注册
0. 退出系统
```

### 普通用户菜单
```
1. 书籍浏览    — 查看书籍列表、搜索、查看详情
2. 借阅管理    — 借书、还书、查看我的借阅记录
0. 退出登录
```

### 管理员菜单
```
1. 书籍管理    — 增删改查书籍
2. 借阅管理    — 借书、还书、查看全部借阅记录
3. 用户管理    — 查看、编辑、删除用户
4. 退出登录
```

## 数据库表结构

### users 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| username | VARCHAR(50) | 用户名，唯一 |
| password | VARCHAR(255) | BCrypt 加密密码 |
| name | VARCHAR(50) | 姓名 |
| email | VARCHAR(100) | 邮箱 |
| role | ENUM | user/admin |
| status | TINYINT | 1正常/0禁用 |

### books 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| isbn | VARCHAR(20) | ISBN，唯一 |
| title | VARCHAR(200) | 书名 |
| author | VARCHAR(100) | 作者 |
| category | VARCHAR(50) | 分类 |
| location | VARCHAR(50) | 位置 |
| total_quantity | INT | 总库存 |
| available_quantity | INT | 可借数量 |
| description | TEXT | 简介 |

### borrow_records 表
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT | 主键自增 |
| user_id | INT | 用户ID，外键 |
| book_id | INT | 书籍ID，外键 |
| borrow_date | DATETIME | 借阅日期 |
| due_date | DATETIME | 应还日期（30天） |
| return_date | DATETIME | 实际归还日期 |
| status | ENUM | borrowing/returned |
