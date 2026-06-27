package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.dao.UserDao;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class BorrowService {

    private static final Logger log = LoggerFactory.getLogger(BorrowService.class);

    @Autowired
    private BorrowRecordDao borrowRecordDao;

    @Autowired
    private BookDao bookDao;

    @Autowired
    private UserDao userDao;

    public PageResponse<BorrowRecord> getMyBorrowRecords(Long userId, String status, int page, int pageSize) {
        long total = borrowRecordDao.count(status, userId);
        List<BorrowRecord> records = borrowRecordDao.findAll(status, userId, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }

    public PageResponse<BorrowRecord> getAllBorrowRecords(String status, Long userId, int page, int pageSize) {
        long total = borrowRecordDao.count(status, userId);
        List<BorrowRecord> records = borrowRecordDao.findAll(status, userId, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }

    public void borrowBook(Long userId, Long bookId) {
        Book book = bookDao.findById(bookId);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        if (book.getAvailableQuantity() <= 0) {
            throw new BusinessException(400, "库存不足");
        }

        if (borrowRecordDao.existsActiveBorrow(userId, bookId)) {
            throw new BusinessException(400, "已借阅该书");
        }

        BorrowRecord record = new BorrowRecord();
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowDate(LocalDateTime.now());
        record.setDueDate(LocalDateTime.now().plusDays(30));
        record.setStatus("borrowing");
        borrowRecordDao.save(record);

        bookDao.decreaseAvailable(bookId);

        log.info("借书成功: userId={}, bookId={}", userId, bookId);
    }

    public void returnBook(Long userId, Long recordId, String userRole) {
        BorrowRecord record = borrowRecordDao.findById(recordId);
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
        borrowRecordDao.update(record);

        bookDao.increaseAvailable(record.getBookId());

        log.info("还书成功: userId={}, recordId={}", userId, recordId);
    }
}
