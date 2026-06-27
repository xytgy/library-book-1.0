package com.library.service;

import com.library.dao.BorrowRecordDao;
import com.library.dao.UserDao;
import com.library.exception.BusinessException;
import com.library.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {

    private final UserDao userDao;
    private final BorrowRecordDao borrowRecordDao;

    public UserService(UserDao userDao, BorrowRecordDao borrowRecordDao) {
        this.userDao = userDao;
        this.borrowRecordDao = borrowRecordDao;
    }

    public Map<String, Object> getUsers(String search, int page, int pageSize) {
        long total = userDao.count(search);
        List<User> records = userDao.findAll(search, page, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    public User getUserById(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    public User updateUser(Long id, String name, String email, String role, Integer status,
                           Long currentUserId, String currentUserRole) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        if (!id.equals(currentUserId) && !"admin".equals(currentUserRole)) {
            throw new BusinessException(403, "无权修改其他用户信息");
        }

        if (name != null) user.setName(name);
        if (email != null) user.setEmail(email);

        if ("admin".equals(currentUserRole)) {
            if (role != null) user.setRole(role);
            if (status != null) user.setStatus(status);
        }

        userDao.update(user);
        return user;
    }

    public void deleteUser(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (borrowRecordDao.existsActiveBorrowByUserId(id)) {
            throw new BusinessException(400, "该用户有未归还的借阅记录，无法删除");
        }
        userDao.deleteById(id);
    }
}
