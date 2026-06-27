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
