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