package com.library.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.library.dao.UserDao;
import com.library.exception.BusinessException;
import com.library.model.User;
import com.library.util.JwtUtil;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private final UserDao userDao;
    private final JwtUtil jwtUtil;

    public AuthService(UserDao userDao, JwtUtil jwtUtil) {
        this.userDao = userDao;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
        if (!result.verified) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", toUserInfo(user));
        return response;
    }

    public Map<String, Object> register(String username, String password, String name, String email) {
        if (userDao.existsByUsername(username)) {
            throw new BusinessException(400, "用户名已存在");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(BCrypt.withDefaults().hashToString(12, password.toCharArray()));
        user.setName(name);
        user.setEmail(email);
        user.setRole("user");
        user.setStatus(1);
        userDao.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", toUserInfo(user));
        return response;
    }

    private Map<String, Object> toUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("name", user.getName());
        info.put("email", user.getEmail());
        info.put("role", user.getRole());
        return info;
    }
}
