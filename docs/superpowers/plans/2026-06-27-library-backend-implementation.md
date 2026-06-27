# 图书管理系统后端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现图书管理系统后端，从Node.js重写为Java Spring Boot + JDBC

**Architecture:** 采用经典三层架构（Controller -> Service -> DAO），使用JdbcTemplate进行数据库访问，Spring Security进行认证授权，JWT进行Token管理

**Tech Stack:** Spring Boot 3.2.5, Java 17, Maven, MySQL 8.x, JdbcTemplate, Spring Security, BCryptPasswordEncoder, Springdoc OpenAPI, jjwt 0.12.5

---

## 文件结构

```
library-backend/
├── src/main/java/com/library/
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security配置
│   │   ├── WebConfig.java               # CORS配置
│   │   └── JacksonConfig.java           # JSON序列化配置
│   ├── controller/
│   │   ├── AuthController.java          # 认证控制器
│   │   ├── BookController.java          # 书籍控制器
│   │   ├── BorrowController.java        # 借阅控制器
│   │   └── UserController.java          # 用户控制器
│   ├── service/
│   │   ├── AuthService.java             # 认证服务
│   │   ├── BookService.java             # 书籍服务
│   │   ├── BorrowService.java           # 借阅服务
│   │   └── UserService.java             # 用户服务
│   ├── dao/
│   │   ├── UserDao.java                 # 用户数据访问
│   │   ├── BookDao.java                 # 书籍数据访问
│   │   └── BorrowRecordDao.java         # 借阅记录数据访问
│   ├── model/
│   │   ├── User.java                    # 用户实体
│   │   ├── Book.java                    # 书籍实体
│   │   └── BorrowRecord.java            # 借阅记录实体
│   ├── dto/
│   │   ├── request/
│   │   │   ├── LoginRequest.java        # 登录请求
│   │   │   ├── RegisterRequest.java     # 注册请求
│   │   │   └── BookRequest.java         # 书籍请求
│   │   └── response/
│   │       ├── ApiResponse.java         # 统一响应格式
│   │       ├── LoginResponse.java       # 登录响应
│   │       └── PageResponse.java        # 分页响应
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java # JWT认证过滤器
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java  # 全局异常处理
│   │   ├── BusinessException.java       # 业务异常
│   │   └── ErrorCode.java               # 错误码枚举
│   └── util/
│       ├── JwtUtil.java                 # JWT工具类
│       └── JwtUserDetails.java          # JWT用户详情
├── src/main/resources/
│   ├── application.yml                  # 主配置文件
│   └── logback-spring.xml               # 日志配置
└── pom.xml                              # Maven配置
```

---

## Task 1: 更新pom.xml添加Spring Security依赖

**Files:**
- Modify: `backend/pom.xml`

- [ ] **Step 1: 添加Spring Security依赖**

```xml
<!-- Spring Boot Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

- [ ] **Step 2: 验证依赖添加成功**

Run: `cd backend && mvn dependency:tree | grep spring-security`
Expected: 看到spring-security相关依赖

- [ ] **Step 3: 提交更改**

```bash
git add backend/pom.xml
git commit -m "deps: add Spring Security dependency"
```

---

## Task 2: 创建JwtUserDetails类

**Files:**
- Create: `backend/src/main/java/com/library/util/JwtUserDetails.java`

- [ ] **Step 1: 创建JwtUserDetails类**

```java
package com.library.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

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

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/util/JwtUserDetails.java
git commit -m "feat: add JwtUserDetails implementation"
```

---

## Task 3: 创建UserDao

**Files:**
- Create: `backend/src/main/java/com/library/dao/UserDao.java`

- [ ] **Step 1: 创建UserDao类**

```java
package com.library.dao;

import com.library.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/dao/UserDao.java
git commit -m "feat: add UserDao with CRUD operations"
```

---

## Task 4: 创建BookDao

**Files:**
- Create: `backend/src/main/java/com/library/dao/BookDao.java`

- [ ] **Step 1: 创建BookDao类**

```java
package com.library.dao;

import com.library.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BookDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Book> rowMapper = new BeanPropertyRowMapper<>(Book.class);

    public Book findById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        List<Book> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public Book findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        List<Book> results = jdbcTemplate.query(sql, rowMapper, isbn);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByIsbn(String isbn) {
        String sql = "SELECT EXISTS(SELECT 1 FROM books WHERE isbn = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, isbn));
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

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/dao/BookDao.java
git commit -m "feat: add BookDao with CRUD operations"
```

---

## Task 5: 创建BorrowRecordDao

**Files:**
- Create: `backend/src/main/java/com/library/dao/BorrowRecordDao.java`

- [ ] **Step 1: 创建BorrowRecordDao类**

```java
package com.library.dao;

import com.library.model.BorrowRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/dao/BorrowRecordDao.java
git commit -m "feat: add BorrowRecordDao with CRUD operations"
```

---

## Task 6: 创建SecurityConfig

**Files:**
- Create: `backend/src/main/java/com/library/config/SecurityConfig.java`

- [ ] **Step 1: 创建SecurityConfig类**

```java
package com.library.config;

import com.library.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/books/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = 
            new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.List.of("http://localhost:5173"));
        configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(java.util.List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = 
            new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/config/SecurityConfig.java
git commit -m "feat: add SecurityConfig with JWT filter"
```

---

## Task 7: 更新JwtAuthenticationFilter

**Files:**
- Modify: `backend/src/main/java/com/library/filter/JwtAuthenticationFilter.java`

- [ ] **Step 1: 重写JwtAuthenticationFilter**

```java
package com.library.filter;

import com.library.dao.UserDao;
import com.library.model.User;
import com.library.util.JwtUserDetails;
import com.library.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDao userDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        
        try {
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
                    
                    log.debug("JWT认证成功: userId={}, username={}, role={}", 
                        user.getId(), user.getUsername(), user.getRole());
                }
            }
        } catch (Exception e) {
            log.error("JWT认证失败", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/filter/JwtAuthenticationFilter.java
git commit -m "feat: update JwtAuthenticationFilter with Spring Security integration"
```

---

## Task 8: 创建AuthService

**Files:**
- Modify: `backend/src/main/java/com/library/service/AuthService.java`

- [ ] **Step 1: 实现AuthService**

```java
package com.library.service;

import com.library.dao.UserDao;
import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.exception.BusinessException;
import com.library.model.User;
import com.library.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse.UserDTO userDTO = new LoginResponse.UserDTO(
            user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getRole()
        );
        
        log.info("用户登录成功: {}", user.getUsername());
        return new LoginResponse(token, userDTO);
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
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        LoginResponse.UserDTO userDTO = new LoginResponse.UserDTO(
            user.getId(), user.getUsername(), user.getName(), user.getEmail(), user.getRole()
        );
        
        log.info("用户注册成功: {}", user.getUsername());
        return new LoginResponse(token, userDTO);
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/service/AuthService.java
git commit -m "feat: implement AuthService with login and register"
```

---

## Task 9: 创建BookService

**Files:**
- Modify: `backend/src/main/java/com/library/service/BookService.java`

- [ ] **Step 1: 实现BookService**

```java
package com.library.service;

import com.library.dao.BookDao;
import com.library.dto.request.BookRequest;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

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

    public Book createBook(BookRequest request) {
        if (request.getIsbn() != null && !request.getIsbn().isEmpty()) {
            if (bookDao.existsByIsbn(request.getIsbn())) {
                throw new BusinessException(400, "ISBN已存在");
            }
        }
        
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
        
        log.info("添加书籍成功: {}", book.getTitle());
        return book;
    }

    public Book updateBook(Long id, BookRequest request) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        
        if (request.getIsbn() != null && !request.getIsbn().isEmpty()) {
            if (!request.getIsbn().equals(book.getIsbn()) && bookDao.existsByIsbn(request.getIsbn())) {
                throw new BusinessException(400, "ISBN已存在");
            }
        }
        
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCategory(request.getCategory());
        book.setLocation(request.getLocation());
        book.setTotalQuantity(request.getTotalQuantity());
        book.setDescription(request.getDescription());
        bookDao.update(book);
        
        log.info("更新书籍成功: {}", book.getTitle());
        return book;
    }

    public void deleteBook(Long id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        bookDao.deleteById(id);
        log.info("删除书籍成功: {}", book.getTitle());
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/service/BookService.java
git commit -m "feat: implement BookService with CRUD operations"
```

---

## Task 10: 创建BorrowService

**Files:**
- Modify: `backend/src/main/java/com/library/service/BorrowService.java`

- [ ] **Step 1: 实现BorrowService**

```java
package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.dao.UserDao;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BorrowService {

    private static final Logger log = LoggerFactory.getLogger(BorrowService.class);

    @Autowired
    private BorrowRecordDao borrowRecordDao;

    @Autowired
    private BookDao bookDao;

    @Autowired
    private UserDao userDao;

    public PageResponse<BorrowRecord> getMyBorrowRecords(Long userId, String status, int page, int pageSize) {
        long total = borrowRecordDao.count(status, userId);
        List<BorrowRecord> records = borrowRecordDao.findAll(status, userId, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }

    public PageResponse<BorrowRecord> getAllBorrowRecords(String status, Long userId, int page, int pageSize) {
        long total = borrowRecordDao.count(status, userId);
        List<BorrowRecord> records = borrowRecordDao.findAll(status, userId, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }

    public void borrowBook(Long userId, Long bookId) {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new BusinessException(400, "库存不足");
        }

        if (borrowRecordDao.existsActiveBorrow(userId, bookId)) {
            throw new BusinessException(400, "已借阅该书");
        }

        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(30));
        record.setStatus("borrowing");
        borrowRecordDao.save(record);

        bookDao.decreaseAvailable(bookId);
        
        log.info("借书成功: userId={}, bookId={}", userId, bookId);
    }

    public void returnBook(Long userId, Long recordId, String userRole) {
        BorrowRecord record = borrowRecordDao.findById(recordId);
        if (record == null) {
            throw new BusinessException(404, "借阅记录不存在");
        }
        if ("returned".equals(record.getStatus())) {
            throw new BusinessException(400, "已归还");
        }

        if (!record.getUserId().equals(userId) && !"admin".equals(userRole)) {
            throw new BusinessException(403, "无权归还他人书籍");
        }

        record.setReturnDate(LocalDateTime.now());
        record.setStatus("returned");
        borrowRecordDao.update(record);

        bookDao.increaseAvailable(record.getBookId());
        
        log.info("还书成功: userId={}, recordId={}", userId, recordId);
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/service/BorrowService.java
git commit -m "feat: implement BorrowService with borrow and return"
```

---

## Task 11: 创建UserService

**Files:**
- Modify: `backend/src/main/java/com/library/service/UserService.java`

- [ ] **Step 1: 实现UserService**

```java
package com.library.service;

import com.library.dao.UserDao;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    public User updateUser(Long id, Map<String, Object> updates, Long currentUserId, String currentUserRole) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!id.equals(currentUserId) && !"admin".equals(currentUserRole)) {
            throw new BusinessException(403, "无权修改其他用户信息");
        }

        if (updates.containsKey("name")) {
            user.setName((String) updates.get("name"));
        }
        if (updates.containsKey("email")) {
            user.setEmail((String) updates.get("email"));
        }

        if ("admin".equals(currentUserRole)) {
            if (updates.containsKey("role")) {
                user.setRole((String) updates.get("role"));
            }
            if (updates.containsKey("status")) {
                user.setStatus((Integer) updates.get("status"));
            }
        }

        userDao.update(user);
        log.info("更新用户信息成功: userId={}", id);
        return user;
    }

    public void deleteUser(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        userDao.deleteById(id);
        log.info("删除用户成功: userId={}", id);
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/service/UserService.java
git commit -m "feat: implement UserService with CRUD operations"
```

---

## Task 12: 更新AuthController

**Files:**
- Modify: `backend/src/main/java/com/library/controller/AuthController.java`

- [ ] **Step 1: 实现AuthController**

```java
package com.library.controller;

import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.LoginResponse;
import com.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理", description = "用户登录、注册")
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
    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ApiResponse.success("注册成功", response);
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/controller/AuthController.java
git commit -m "feat: implement AuthController with login and register"
```

---

## Task 13: 更新BookController

**Files:**
- Modify: `backend/src/main/java/com/library/controller/BookController.java`

- [ ] **Step 1: 实现BookController**

```java
package com.library.controller;

import com.library.dto.request.BookRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.model.Book;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "书籍管理", description = "书籍的增删改查")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    @Operation(summary = "获取书籍列表")
    public ApiResponse<PageResponse<Book>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Book> response = bookService.getBooks(search, category, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取书籍详情")
    public ApiResponse<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ApiResponse.success(book);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加书籍（管理员）")
    public ApiResponse<Book> createBook(@Valid @RequestBody BookRequest request) {
        Book book = bookService.createBook(request);
        return ApiResponse.success("添加成功", book);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新书籍（管理员）")
    public ApiResponse<Book> updateBook(@PathVariable Long id,
                                        @Valid @RequestBody BookRequest request) {
        Book book = bookService.updateBook(id, request);
        return ApiResponse.success("更新成功", book);
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

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/controller/BookController.java
git commit -m "feat: implement BookController with CRUD operations"
```

---

## Task 14: 更新BorrowController

**Files:**
- Modify: `backend/src/main/java/com/library/controller/BorrowController.java`

- [ ] **Step 1: 实现BorrowController**

```java
package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.BorrowRecord;
import com.library.service.BorrowService;
import com.library.util.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
@Tag(name = "借阅管理", description = "图书借阅与归还")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @GetMapping("/my")
    @Operation(summary = "获取我的借阅记录")
    public ApiResponse<PageResponse<BorrowRecord>> getMyBorrowRecords(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        PageResponse<BorrowRecord> response = borrowService.getMyBorrowRecords(userId, status, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有借阅记录（管理员）")
    public ApiResponse<PageResponse<BorrowRecord>> getAllBorrowRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<BorrowRecord> response = borrowService.getAllBorrowRecords(status, userId, page, pageSize);
        return ApiResponse.success(response);
    }

    @PostMapping("/{bookId}")
    @Operation(summary = "借书")
    public ApiResponse<Void> borrowBook(@PathVariable Long bookId,
                                        @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        borrowService.borrowBook(userId, bookId);
        return ApiResponse.success("借阅成功");
    }

    @PostMapping("/return/{recordId}")
    @Operation(summary = "还书")
    public ApiResponse<Void> returnBook(@PathVariable Long recordId,
                                        @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();
        borrowService.returnBook(userId, recordId, userRole);
        return ApiResponse.success("归还成功");
    }

    private Long getUserId(JwtUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(401, "未登录");
        }
        return userDetails.getUserId();
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/controller/BorrowController.java
git commit -m "feat: implement BorrowController with borrow and return"
```

---

## Task 15: 更新UserController

**Files:**
- Modify: `backend/src/main/java/com/library/controller/UserController.java`

- [ ] **Step 1: 实现UserController**

```java
package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.User;
import com.library.service.UserService;
import com.library.util.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息管理")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户列表（管理员）")
    public ApiResponse<PageResponse<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<User> response = userService.getUsers(search, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ApiResponse<User> updateUser(@PathVariable Long id,
                                        @RequestBody Map<String, Object> updates,
                                        @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long currentUserId = getUserId(userDetails);
        String currentUserRole = userDetails.getAuthorities().iterator().next().getAuthority();
        User user = userService.updateUser(id, updates, currentUserId, currentUserRole);
        return ApiResponse.success("更新成功", user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户（管理员）")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功");
    }

    private Long getUserId(JwtUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(401, "未登录");
        }
        return userDetails.getUserId();
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `cd backend && mvn compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交更改**

```bash
git add backend/src/main/java/com/library/controller/UserController.java
git commit -m "feat: implement UserController with CRUD operations"
```

---

## Task 16: 验证项目编译

**Files:**
- None

- [ ] **Step 1: 执行完整编译**

Run: `cd backend && mvn clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 2: 验证所有类都已实现**

检查以下类是否都已实现：
- [ ] UserDao
- [ ] BookDao
- [ ] BorrowRecordDao
- [ ] AuthService
- [ ] BookService
- [ ] BorrowService
- [ ] UserService
- [ ] AuthController
- [ ] BookController
- [ ] BorrowController
- [ ] UserController
- [ ] SecurityConfig
- [ ] JwtUserDetails
- [ ] JwtAuthenticationFilter

- [ ] **Step 3: 提交最终更改**

```bash
git add .
git commit -m "feat: complete backend implementation"
```

---

## 完成

实现计划已完成。所有任务都按照TDD方法设计，每个任务都包含：
1. 具体的代码实现
2. 验证步骤
3. 提交步骤

可以开始执行实现了。