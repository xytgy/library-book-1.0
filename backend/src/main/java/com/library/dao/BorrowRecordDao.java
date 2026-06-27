package com.library.dao;

import com.library.db.DatabaseUtil;
import com.library.model.BorrowRecord;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowRecordDao {

    public BorrowRecord findById(Long id) {
        String sql = "SELECT br.*, b.title as book_title, b.author as book_author, u.name as user_name " +
                    "FROM borrow_records br " +
                    "LEFT JOIN books b ON br.book_id = b.id " +
                    "LEFT JOIN users u ON br.user_id = u.id " +
                    "WHERE br.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            throw new RuntimeException("查询借阅记录失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return null;
    }

    public BorrowRecord findByIdForUpdate(Connection conn, Long id) throws SQLException {
        String sql = "SELECT br.*, b.title as book_title, b.author as book_author, u.name as user_name " +
                    "FROM borrow_records br " +
                    "LEFT JOIN books b ON br.book_id = b.id " +
                    "LEFT JOIN users u ON br.user_id = u.id " +
                    "WHERE br.id = ? FOR UPDATE";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
        return null;
    }

    public boolean existsActiveBorrow(Long userId, Long bookId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM borrow_records WHERE user_id = ? AND book_id = ? AND status = 'borrowing')";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("查询借阅记录失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return false;
    }

    public boolean existsActiveBorrow(Connection conn, Long userId, Long bookId) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM borrow_records WHERE user_id = ? AND book_id = ? AND status = 'borrowing')";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            ps.setLong(2, bookId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean(1);
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }
        return false;
    }

    public boolean existsActiveBorrowByBookId(Long bookId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM borrow_records WHERE book_id = ? AND status = 'borrowing')";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, bookId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("查询借阅记录失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return false;
    }

    public boolean existsActiveBorrowByUserId(Long userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM borrow_records WHERE user_id = ? AND status = 'borrowing')";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("查询借阅记录失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return false;
    }

    public int save(BorrowRecord record) {
        String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.getUserId());
            ps.setLong(2, record.getBookId());
            ps.setTimestamp(3, Timestamp.valueOf(record.getBorrowDate()));
            ps.setTimestamp(4, Timestamp.valueOf(record.getDueDate()));
            ps.setString(5, record.getStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) record.setId(keys.getLong(1));
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("保存借阅记录失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, null);
        }
    }

    public int save(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "INSERT INTO borrow_records (user_id, book_id, borrow_date, due_date, status) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, record.getUserId());
            ps.setLong(2, record.getBookId());
            ps.setTimestamp(3, Timestamp.valueOf(record.getBorrowDate()));
            ps.setTimestamp(4, Timestamp.valueOf(record.getDueDate()));
            ps.setString(5, record.getStatus());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) record.setId(keys.getLong(1));
            }
            return rows;
        } finally {
            if (ps != null) ps.close();
        }
    }

    public int update(BorrowRecord record) {
        String sql = "UPDATE borrow_records SET return_date=?, status=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            if (record.getReturnDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(record.getReturnDate()));
            } else {
                ps.setNull(1, Types.TIMESTAMP);
            }
            ps.setString(2, record.getStatus());
            ps.setLong(3, record.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新借阅记录失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, null);
        }
    }

    public int update(Connection conn, BorrowRecord record) throws SQLException {
        String sql = "UPDATE borrow_records SET return_date=?, status=? WHERE id=?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            if (record.getReturnDate() != null) {
                ps.setTimestamp(1, Timestamp.valueOf(record.getReturnDate()));
            } else {
                ps.setNull(1, Types.TIMESTAMP);
            }
            ps.setString(2, record.getStatus());
            ps.setLong(3, record.getId());
            return ps.executeUpdate();
        } finally {
            if (ps != null) ps.close();
        }
    }

    public List<BorrowRecord> findAll(String status, Long userId, int page, int pageSize) {
        StringBuilder sql = new StringBuilder(
            "SELECT br.*, b.title as book_title, b.author as book_author, u.name as user_name " +
            "FROM borrow_records br " +
            "LEFT JOIN books b ON br.book_id = b.id " +
            "LEFT JOIN users u ON br.user_id = u.id " +
            "WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendConditions(sql, params, status, userId);

        sql.append(" ORDER BY br.id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<BorrowRecord> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            setParams(ps, params);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("查询借阅记录列表失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return list;
    }

    public long count(String status, Long userId) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM borrow_records WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendConditions(sql, params, status, userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql.toString());
            setParams(ps, params);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("查询借阅记录数量失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return 0;
    }

    private void appendConditions(StringBuilder sql, List<Object> params, String status, Long userId) {
        if (status != null && !status.isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }
        if (userId != null) {
            sql.append(" AND user_id = ?");
            params.add(userId);
        }
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String) ps.setString(i + 1, (String) param);
            else if (param instanceof Integer) ps.setInt(i + 1, (Integer) param);
            else if (param instanceof Long) ps.setLong(i + 1, (Long) param);
        }
    }

    private BorrowRecord mapRow(ResultSet rs) throws SQLException {
        BorrowRecord record = new BorrowRecord();
        record.setId(rs.getLong("id"));
        record.setUserId(rs.getLong("user_id"));
        record.setBookId(rs.getLong("book_id"));
        Timestamp borrowDate = rs.getTimestamp("borrow_date");
        Timestamp dueDate = rs.getTimestamp("due_date");
        Timestamp returnDate = rs.getTimestamp("return_date");
        if (borrowDate != null) record.setBorrowDate(borrowDate.toLocalDateTime());
        if (dueDate != null) record.setDueDate(dueDate.toLocalDateTime());
        if (returnDate != null) record.setReturnDate(returnDate.toLocalDateTime());
        record.setStatus(rs.getString("status"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null) record.setCreatedAt(createdAt.toLocalDateTime());
        if (updatedAt != null) record.setUpdatedAt(updatedAt.toLocalDateTime());
        record.setBookTitle(rs.getString("book_title"));
        record.setBookAuthor(rs.getString("book_author"));
        record.setUserName(rs.getString("user_name"));
        return record;
    }
}
