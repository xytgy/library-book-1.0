package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.BorrowRecordDao;
import com.library.exception.BusinessException;
import com.library.model.Book;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookService {

    private final BookDao bookDao;
    private final BorrowRecordDao borrowRecordDao;

    public BookService(BookDao bookDao, BorrowRecordDao borrowRecordDao) {
        this.bookDao = bookDao;
        this.borrowRecordDao = borrowRecordDao;
    }

    public Map<String, Object> getBooks(String search, String category, int page, int pageSize) {
        long total = bookDao.count(search, category);
        List<Book> records = bookDao.findAll(search, category, page, pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    public Book getBookById(Long id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        return book;
    }

    public Book createBook(String isbn, String title, String author, String category,
                           String location, Integer totalQuantity, String description) {
        if (isbn != null && !isbn.isEmpty()) {
            if (bookDao.existsByIsbn(isbn)) {
                throw new BusinessException(400, "ISBN已存在");
            }
        }

        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setLocation(location);
        book.setTotalQuantity(totalQuantity);
        book.setAvailableQuantity(totalQuantity);
        book.setDescription(description);
        bookDao.save(book);
        return book;
    }

    public Book updateBook(Long id, String isbn, String title, String author, String category,
                           String location, Integer totalQuantity, String description) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }

        if (isbn != null && !isbn.isEmpty()) {
            if (!isbn.equals(book.getIsbn()) && bookDao.existsByIsbn(isbn)) {
                throw new BusinessException(400, "ISBN已存在");
            }
        }

        int oldTotal = book.getTotalQuantity();
        int newTotal = totalQuantity;
        int delta = newTotal - oldTotal;

        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setLocation(location);
        book.setTotalQuantity(newTotal);
        book.setAvailableQuantity(Math.max(0, book.getAvailableQuantity() + delta));
        book.setDescription(description);
        bookDao.update(book);
        return book;
    }

    public void deleteBook(Long id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        if (borrowRecordDao.existsActiveBorrowByBookId(id)) {
            throw new BusinessException(400, "该书籍有未归还的借阅记录，无法删除");
        }
        bookDao.deleteById(id);
    }
}
