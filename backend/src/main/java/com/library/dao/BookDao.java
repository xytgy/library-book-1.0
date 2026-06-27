package com.library.dao;

import com.library.db.DatabaseUtil;
import com.library.model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    public Book findById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";
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
            throw new RuntimeException("查询书籍失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return null;
    }

    public Book findByIdForUpdate(Connection conn, Long id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ? FOR UPDATE";
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

    public boolean existsByIsbn(String isbn) {
        String sql = "SELECT EXISTS(SELECT 1 FROM books WHERE isbn = ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, isbn);
            rs = ps.executeQuery();
            if (rs.next()) return rs.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException("查询书籍失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return false;
    }

    public int save(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, category, location, total_quantity, available_quantity, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setString(5, book.getLocation());
            ps.setInt(6, book.getTotalQuantity());
            ps.setInt(7, book.getAvailableQuantity());
            ps.setString(8, book.getDescription());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) book.setId(keys.getLong(1));
            }
            return rows;
        } catch (SQLException e) {
            throw new RuntimeException("保存书籍失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, null);
        }
    }

    public int update(Book book) {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, category=?, location=?, total_quantity=?, available_quantity=?, description=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setString(5, book.getLocation());
            ps.setInt(6, book.getTotalQuantity());
            ps.setInt(7, book.getAvailableQuantity());
            ps.setString(8, book.getDescription());
            ps.setLong(9, book.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新书籍失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, null);
        }
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除书籍失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, null);
        }
    }

    public boolean decreaseAvailable(Long bookId) {
        String sql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ? AND available_quantity > 0";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("扣减库存失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, null);
        }
    }

    public boolean decreaseAvailable(Connection conn, Long bookId) throws SQLException {
        String sql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ? AND available_quantity > 0";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) ps.close();
        }
    }

    public boolean increaseAvailable(Long bookId) {
        String sql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("增加库存失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, null);
        }
    }

    public boolean increaseAvailable(Connection conn, Long bookId) throws SQLException {
        String sql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE id = ?";
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setLong(1, bookId);
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) ps.close();
        }
    }

    public List<Book> findAll(String search, String category, int page, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendConditions(sql, params, search, category);

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);

        List<Book> list = new ArrayList<>();
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
            throw new RuntimeException("查询书籍列表失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return list;
    }

    public long count(String search, String category) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();

        appendConditions(sql, params, search, category);

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
            throw new RuntimeException("查询书籍数量失败", e);
        } finally {
            DatabaseUtil.close(conn, ps, rs);
        }
        return 0;
    }

    private void appendConditions(StringBuilder sql, List<Object> params, String search, String category) {
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
    }

    private void setParams(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            if (param instanceof String) ps.setString(i + 1, (String) param);
            else if (param instanceof Integer) ps.setInt(i + 1, (Integer) param);
            else if (param instanceof Long) ps.setLong(i + 1, (Long) param);
        }
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setId(rs.getLong("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setCategory(rs.getString("category"));
        book.setLocation(rs.getString("location"));
        book.setTotalQuantity(rs.getInt("total_quantity"));
        book.setAvailableQuantity(rs.getInt("available_quantity"));
        book.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (createdAt != null) book.setCreatedAt(createdAt.toLocalDateTime());
        if (updatedAt != null) book.setUpdatedAt(updatedAt.toLocalDateTime());
        return book;
    }
}
