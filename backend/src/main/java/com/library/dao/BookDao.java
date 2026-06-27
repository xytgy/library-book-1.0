package com.library.dao;

import com.library.model.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BookDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Book> rowMapper = new BeanPropertyRowMapper<>(Book.class);

    public Book findById(Long id) {
        String sql = "SELECT * FROM books WHERE id = ?";
        List<Book> results = jdbcTemplate.query(sql, rowMapper, id);
        return results.isEmpty() ? null : results.get(0);
    }

    public Book findByIsbn(String isbn) {
        String sql = "SELECT * FROM books WHERE isbn = ?";
        List<Book> results = jdbcTemplate.query(sql, rowMapper, isbn);
        return results.isEmpty() ? null : results.get(0);
    }

    public boolean existsByIsbn(String isbn) {
        String sql = "SELECT EXISTS(SELECT 1 FROM books WHERE isbn = ?)";
        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, isbn));
    }

    public int save(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, category, location, total_quantity, available_quantity, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        return jdbcTemplate.update(sql, book.getIsbn(), book.getTitle(), book.getAuthor(), 
            book.getCategory(), book.getLocation(), book.getTotalQuantity(), 
            book.getAvailableQuantity(), book.getDescription());
    }

    public int update(Book book) {
        String sql = "UPDATE books SET isbn=?, title=?, author=?, category=?, location=?, total_quantity=?, available_quantity=?, description=? WHERE id=?";
        return jdbcTemplate.update(sql, book.getIsbn(), book.getTitle(), book.getAuthor(),
            book.getCategory(), book.getLocation(), book.getTotalQuantity(),
            book.getAvailableQuantity(), book.getDescription(), book.getId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM books WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }

    public int decreaseAvailable(Long bookId) {
        String sql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE id = ? AND available_quantity > 0";
        return jdbcTemplate.update(sql, bookId);
    }

    public int increaseAvailable(Long bookId) {
        String sql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE id = ?";
        return jdbcTemplate.update(sql, bookId);
    }

    public List<Book> findAll(String search, String category, int page, int pageSize) {
        StringBuilder sql = new StringBuilder("SELECT * FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
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
        
        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(pageSize);
        params.add((page - 1) * pageSize);
        
        return jdbcTemplate.query(sql.toString(), rowMapper, params.toArray());
    }

    public long count(String search, String category) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM books WHERE 1=1");
        List<Object> params = new ArrayList<>();
        
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
        
        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }
}