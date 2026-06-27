# 图书管理系统后端实现设计规范

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
| 后端框架 | Spring Boot | 3.2.5 |
| Java 版本 | OpenJDK | 17 LTS |
| 构建工具 | Maven | 3.9+ |
| 数据库 | MySQL | 8.x |
| 数据库连接 | JdbcTemplate | - |
| 安全框架 | Spring Security | 6.x |
| 密码加密 | BCryptPasswordEncoder | - |
| API 文档 | Springdoc OpenAPI | 2.x |
| 日志 | SLF4J + Logback | - |
| 参数校验 | Jakarta Bean Validation | 3.x |
| JWT | jjwt | 0.12.5 |

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
│              Spring Security Filter Chain                │
│         (JwtAuthFilter + AuthenticationFilter)          │
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
│           (JdbcTemplate 数据访问)                        │
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
│   │   │   │   ├── SecurityConfig.java          # Spring Security 配置
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
│   │   │   │   └── JwtAuthenticationFilter.java # JWT 认证过滤器
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

### 5.1 Spring Security 配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 5.2 CORS 配置

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

### 5.3 JWT 认证

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

### 5.4 密码加密

使用 BCryptPasswordEncoder，salt rounds = 10。

### 5.5 全局异常处理

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

## 6. 实现顺序

### 6.1 从底层到上层

按照从底层到上层的顺序实现：

```
DAO层 → Service层 → Controller层
```

### 6.2 DAO层设计

使用 JdbcTemplate + BeanPropertyRowMapper 实现数据访问：

```java
@Repository
public class UserDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<User> rowMapper = new BeanPropertyRowMapper<>(User.class);

    public User findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        List<User> results = jdbcTemplate.query(sql, rowMapper, username);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByUsername(String username) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE username = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, username));
    }

    public int save(User user) {
        String sql = "INSERT INTO users (username, password, name, email, role, status) VALUES (?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), 
            user.getName(), user.getEmail(), user.getRole(), user.getStatus());
    }

    public int update(User user) {
        String sql = "UPDATE users SET name=?, email=?, role=?, status=? WHERE id=?";
        return jdbcTemplate.update(sql, user.getName(), user.getEmail(), 
            user.getRole(), user.getStatus(), user.getId());
    }

    public int updatePassword(Long id, String encodedPassword) {
        String sql = "UPDATE users SET password=? WHERE id=?";
        return jdbcTemplate.update(sql, encodedPassword, id);
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public List<User> findAll(String search, int page, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (username LIKE ? OR name LIKE ? OR email LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public long count(String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (username LIKE ? OR name LIKE ? OR email LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }
}
```

```java
@Repository
public class BorrowRecordDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<BorrowRecord> rowMapper = new BeanPropertyRowMapper<>(BorrowRecord.class);

    public BorrowRecord findById(Long id) {
        String sql = "SELECT br.*, b.title as book_title, b.author as book_author, u.name as user_name " +
                    "FROM borrow_records br " +
                    "LEFT JOIN books b ON br.book_id = b.id " +
                    "LEFT JOIN users u ON br.user_id = u.id " +
                    "WHERE br.id = ?";
        List<BorrowRecord> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsActiveBorrow(Long userId, Long bookId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM borrow_records WHERE user_id = ? AND book_id = ? AND status = 'borrowing')";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, userId, bookId));
    }

    public int save(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, record.getUserId(), record.getBookId(), 
            record.getBorrowDate(), record.getDueDate(), record.getStatus());
    }

    public int update(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET return_date=?, status=? WHERE id=?";
        return jdbcTemplate.update(sql, record.getReturnDate(), record.getStatus(), record.getId());
    }

    public List<BorrowRecord> findByUserId(Long userId, int page, int pageSize) {
        String sql = "SELECT br.*, b.title as book_title, b.author as book_author, u.name as user_name " +
                    "FROM borrow_records br " +
                    "LEFT JOIN books b ON br.book_id = b.id " +
                    "LEFT JOIN users u ON br.user_id = u.id " +
                    "WHERE br.user_id = ? " +
                    "ORDER BY br.id DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, rowMapper, userId, pageSize, (page - 1) * pageSize);
    }

    public long countByUserId(Long userId) {
        String sql = "SELECT COUNT(*) FROM borrow_records WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, userId);
    }

    public List<BorrowRecord> findAll(String status, Long userId, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT br.*, b.title as book_title, b.author as book_author, u.name as user_name " +
            "FROM borrow_records br " +
            "LEFT JOIN books b ON br.book_id = b.id " +
            "LEFT JOIN users u ON br.user_id = u.id " +
            "WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.isEmpty()) {
            sql.append(" AND br.status = ?");
            params.add(status);
        }
        if (userId != null) {
            sql.append(" AND br.user_id = ?");
            params.add(userId);
        }
        
        sql.append(" ORDER BY br.id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public long count(String status, Long userId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM borrow_records WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
        
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }
}
```

```java
@Repository
public class BookDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Book> rowMapper = new BeanPropertyRowMapper<>(Book.class);

    public List<Book> findAll(String search, String category, int page, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (title LIKE ? OR author LIKE ? OR isbn LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public Book findById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        List<Book> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public int save(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, category, location, total_quantity, available_quantity, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, book.getIsbn(), book.getTitle(), book.getAuthor(), 
            book.getCategory(), book.getLocation(), book.getTotalQuantity(), 
            book.getAvailableQuantity(), book.getDescription());
    }

    public int update(Book book) {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, category=?, location=?, total_quantity=?, available_quantity=?, description=? WHERE id=?";
        return jdbcTemplate.update(sql, book.getIsbn(), book.getTitle(), book.getAuthor(),
            book.getCategory(), book.getLocation(), book.getTotalQuantity(),
            book.getAvailableQuantity(), book.getDescription(), book.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int decreaseAvailable(Long bookId) {
        String sql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ? AND available_quantity > 0";
        return jdbcTemplate.update(sql, bookId);
    }

    public int increaseAvailable(Long bookId) {
        String sql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE id = ?";
        return jdbcTemplate.update(sql, bookId);
    }

    public long count(String search, String category) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
        if (search != null && !search.isEmpty()) {
            sql.append(" AND (title LIKE ? OR author LIKE ? OR isbn LIKE ?)");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
            params.add("%" + search + "%");
        }
        if (category != null && !category.isEmpty()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }
}
```

### 6.3 Service层设计

#### AuthService

```java
@Service
@Transactional
public class AuthService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userDao.findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user);
    }

    public LoginResponse register(RegisterRequest request) {
        if (userDao.existsByUsername(request.getUsername())) {
            throw new BusinessException(400, "用户名已存在");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole("user");
        user.setStatus(1);
        userDao.save(user);
        
        String token = jwtUtil.generateToken(user);
        return new LoginResponse(token, user);
    }
}
```

#### BookService

```java
@Service
@Transactional
public class BookService {

    @Autowired
    private BookDao bookDao;

    public PageResponse<Book> getBooks(String search, String category, int page, int pageSize) {
        long total = bookDao.count(search, category);
        List<Book> records = bookDao.findAll(search, category, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }

    public Book getBookById(Long id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        return book;
    }

    public void addBook(BookRequest request) {
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCategory(request.getCategory());
        book.setLocation(request.getLocation());
        book.setTotalQuantity(request.getTotalQuantity());
        book.setAvailableQuantity(request.getTotalQuantity());
        book.setDescription(request.getDescription());
        bookDao.save(book);
    }

    public void updateBook(Long id, BookRequest request) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCategory(request.getCategory());
        book.setLocation(request.getLocation());
        book.setTotalQuantity(request.getTotalQuantity());
        book.setDescription(request.getDescription());
        bookDao.update(book);
    }

    public void deleteBook(Long id) {
        bookDao.deleteById(id);
    }
}
```

#### BorrowService

```java
@Service
@Transactional
public class BorrowService {

    @Autowired
    private BorrowRecordDao borrowRecordDao;

    @Autowired
    private BookDao bookDao;

    @Autowired
    private UserDao userDao;

    public void borrowBook(Long userId, Long bookId) {
        // 1. 检查书籍
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new BusinessException(400, "库存不足");
        }

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

    public void returnBook(Long userId, Long recordId) {
        // 1. 检查借阅记录
        BorrowRecord record = borrowRecordDao.findById(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        if ("returned".equals(record.getStatus())) {
            throw new BusinessException(400, "该书已归还");
        }

        // 2. 检查权限（本人或管理员）
        User user = userDao.findById(userId);
        if (!record.getUserId().equals(userId) && !"admin".equals(user.getRole())) {
            throw new BusinessException(403, "无权归还");
        }

        // 3. 更新借阅记录
        record.setReturnDate(LocalDateTime.now());
        record.setStatus("returned");
        borrowRecordDao.update(record);

        // 4. 增加库存
        bookDao.increaseAvailable(record.getBookId());
    }

    public PageResponse<BorrowRecord> getUserBorrows(Long userId, int page, int pageSize) {
        long total = borrowRecordDao.countByUserId(userId);
        List<BorrowRecord> records = borrowRecordDao.findByUserId(userId, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }
}
```

#### UserService

```java
@Service
@Transactional
public class UserService {

    @Autowired
    private UserDao userDao;

    public PageResponse<User> getUsers(String search, int page, int pageSize) {
        long total = userDao.count(search);
        List<User> records = userDao.findAll(search, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }

    public User getUserById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    public void updateUser(Long id, UserUpdateRequest request, Long currentUserId, String currentRole) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 权限检查
        if (!id.equals(currentUserId) && !"admin".equals(currentRole)) {
            throw new BusinessException(403, "无权修改其他用户信息");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // 管理员可以修改角色和状态
        if ("admin".equals(currentRole)) {
            if (request.getRole() != null) {
                user.setRole(request.getRole());
            }
            if (request.getStatus() != null) {
                user.setStatus(request.getStatus());
            }
        }

        userDao.update(user);
    }
}
```

### 6.4 Controller层设计

#### AuthController

```java
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "登录、注册接口")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.success("注册成功");
    }
}
```

#### BookController

```java
@RestController
@RequestMapping("/api/books")
@Tag(name = "书籍管理", description = "书籍增删改查接口")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    @Operation(summary = "获取书籍列表")
    public ApiResponse<PageResponse<Book>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResponse<Book> response = bookService.getBooks(search, category, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取书籍详情")
    public ApiResponse<Book> getBook(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ApiResponse.success(book);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加书籍（管理员）")
    public ApiResponse<Void> addBook(@Valid @RequestBody BookRequest request) {
        bookService.addBook(request);
        return ApiResponse.success("添加成功");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新书籍（管理员）")
    public ApiResponse<Void> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        bookService.updateBook(id, request);
        return ApiResponse.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除书籍（管理员）")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success("删除成功");
    }
}
```

#### BorrowController

```java
@RestController
@RequestMapping("/api/borrow")
@Tag(name = "借阅管理", description = "借阅、归还接口")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/{bookId}")
    @Operation(summary = "借阅书籍")
    public ApiResponse<Void> borrowBook(@PathVariable Long bookId, 
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        borrowService.borrowBook(userId, bookId);
        return ApiResponse.success("借阅成功");
    }

    @PostMapping("/return/{recordId}")
    @Operation(summary = "归还书籍")
    public ApiResponse<Void> returnBook(@PathVariable Long recordId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        borrowService.returnBook(userId, recordId);
        return ApiResponse.success("归还成功");
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的借阅记录")
    public ApiResponse<PageResponse<BorrowRecord>> getMyBorrows(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails);
        PageResponse<BorrowRecord> response = borrowService.getUserBorrows(userId, page, pageSize);
        return ApiResponse.success(response);
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof JwtUserDetails jwtUserDetails) {
            return jwtUserDetails.getUserId();
        }
        throw new BusinessException(401, "无法获取用户信息");
    }
}
```

#### UserController

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户管理接口")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户列表（管理员）")
    public ApiResponse<PageResponse<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        PageResponse<User> response = userService.getUsers(search, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息")
    public ApiResponse<Void> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long currentUserId = getUserId(userDetails);
        String currentRole = userDetails.getAuthorities().iterator().next().getAuthority();
        userService.updateUser(id, request, currentUserId, currentRole);
        return ApiResponse.success("更新成功");
    }

    private Long getUserId(UserDetails userDetails) {
        if (userDetails instanceof JwtUserDetails jwtUserDetails) {
            return jwtUserDetails.getUserId();
        }
        throw new BusinessException(401, "无法获取用户信息");
    }
}
```

## 7. 关键配置

### 7.0 JwtUserDetails 实现

```java
public class JwtUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String role;
    private final Integer status;

    public JwtUserDetails(Long userId, String username, String password, String role, Integer status) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
    }

    public Long getUserId() { return userId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return status == 1; }
}
```

### 7.1 JwtAuthenticationFilter 更新

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDao userDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        if (jwtUtil.validateToken(token)) {
            Long userId = jwtUtil.getUserId(token);
            User user = userDao.findById(userId);
            
            if (user != null && user.getStatus() == 1) {
                JwtUserDetails userDetails = new JwtUserDetails(
                    user.getId(), user.getUsername(), user.getPassword(), 
                    user.getRole(), user.getStatus()
                );
                
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 7.1 application.yml

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

### 7.2 logback-spring.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/library-backend.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/library-backend.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>${LOG_PATTERN}</pattern>
        </encoder>
    </appender>

    <logger name="com.library" level="DEBUG"/>
    <logger name="org.springframework" level="INFO"/>

    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 7.3 pom.xml 依赖

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>

    <!-- MySQL -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.5</version>
        <scope>runtime</scope>
    </dependency>

    <!-- Springdoc OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.5.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
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
**创建日期**: 2026-06-27
**状态**: 待审核
