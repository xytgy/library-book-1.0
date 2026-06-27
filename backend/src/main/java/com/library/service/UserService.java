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