package com.library.service;

import com.library.dto.request.LoginRequest;
import com.library.dto.request.RegisterRequest;
import com.library.dto.response.LoginResponse;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JdbcTemplate jdbcTemplate;

    public AuthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // TODO: 实现登录逻辑
        // 1. 根据用户名查询用户
        // 2. 验证密码
        // 3. 生成 JWT Token
        // 4. 返回 LoginResponse
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        // TODO: 实现注册逻辑
        // 1. 检查用户名是否已存在
        // 2. 加密密码
        // 3. 插入用户记录
        // 4. 生成 JWT Token
        // 5. 返回 LoginResponse
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }
}
