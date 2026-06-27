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
