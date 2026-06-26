package com.library.service;

import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import com.library.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<User> getUsers(String search, int page, int pageSize) {
        // TODO: 实现查询逻辑
        // 1. 构建查询 SQL（带搜索条件和分页）
        // 2. 执行查询（排除密码字段）
        // 3. 查询总数
        // 4. 返回 PageResponse
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }

    public Map<String, Object> getUserById(Long id) {
        // TODO: 实现详情逻辑
        // 1. 查询用户信息
        // 2. 查询用户的借阅记录
        // 3. 返回用户信息和借阅记录
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    @Transactional
    public User updateUser(Long id, Map<String, Object> updates, Long currentUserId, String currentUserRole) {
        // TODO: 实现更新逻辑
        // 1. 检查用户是否存在
        // 2. 检查权限（普通用户只能修改自己）
        // 3. 更新字段
        // 4. 返回更新后的用户
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }

    @Transactional
    public void deleteUser(Long id) {
        // TODO: 实现删除逻辑
        // 1. 检查用户是否存在
        // 2. 检查是否有未归还的借阅记录
        // 3. 删除用户
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
}
