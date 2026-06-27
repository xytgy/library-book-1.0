package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.db.DatabaseUtil;
import com.library.exception.BusinessException;
import com.library.model.Book;
import com.library.model.BorrowRecord;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BorrowService {

    private final BorrowRecordDao borrowRecordDao;
    private final BookDao bookDao;

    public BorrowService(BorrowRecordDao borrowRecordDao, BookDao bookDao) {
        this.borrowRecordDao = borrowRecordDao;
        this.bookDao = bookDao;
    }

    public Map<String, Object> getMyBorrowRecords(Long userId, String status, int page, int pageSize) {
        long total = borrowRecordDao.count(status, userId);
        List<BorrowRecord> records = borrowRecordDao.findAll(status, userId, page, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    public Map<String, Object> getAllBorrowRecords(String status, Long userId, int page, int pageSize) {
        long total = borrowRecordDao.count(status, userId);
        List<BorrowRecord> records = borrowRecordDao.findAll(status, userId, page, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    public void borrowBook(Long userId, Long bookId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            Book book = bookDao.findByIdForUpdate(conn, bookId);
            if (book == null) {
                throw new BusinessException(404, "书籍不存在");
            }
            if (book.getAvailableQuantity() <= 0) {
                throw new BusinessException(400, "库存不足");
            }

            if (borrowRecordDao.existsActiveBorrow(conn, userId, bookId)) {
                throw new BusinessException(400, "已借阅该书");
            }

            BorrowRecord record = new BorrowRecord();
            record.setUserId(userId);
            record.setBookId(bookId);
            record.setBorrowDate(LocalDateTime.now());
            record.setDueDate(LocalDateTime.now().plusDays(30));
            record.setStatus("borrowing");
            borrowRecordDao.save(conn, record);

            bookDao.decreaseAvailable(conn, bookId);

            conn.commit();
        } catch (BusinessException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("借书失败", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }

    public void returnBook(Long userId, Long recordId, String userRole) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            BorrowRecord record = borrowRecordDao.findByIdForUpdate(conn, recordId);
            if (record == null) {
                throw new BusinessException(404, "借阅记录不存在");
            }
            if ("returned".equals(record.getStatus())) {
                throw new BusinessException(400, "已归还");
            }

            if (!record.getUserId().equals(userId) && !"admin".equals(userRole)) {
                throw new BusinessException(403, "无权归还他人书籍");
            }

            record.setReturnDate(LocalDateTime.now());
            record.setStatus("returned");
            borrowRecordDao.update(conn, record);

            bookDao.increaseAvailable(conn, record.getBookId());

            conn.commit();
        } catch (BusinessException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("还书失败", e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception ignored) {}
            }
        }
    }
}
