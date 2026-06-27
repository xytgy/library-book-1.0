package com.library.service;

import com.library.dao.BookDao;
import com.library.dto.request.BookRequest;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    @Autowired
    private BookDao bookDao;

    public PageResponse<Book> getBooks(String search, String category, int page, int pageSize) {
        long total = bookDao.count(search, category);
        List<Book> records = bookDao.findAll(search, category, page, pageSize);
        return new PageResponse<>(records, total, page, pageSize);
    }

    public Book getBookById(Long id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        return book;
    }

    public Book createBook(BookRequest request) {
        if (request.getIsbn() != null && !request.getIsbn().isEmpty()) {
            if (bookDao.existsByIsbn(request.getIsbn())) {
                throw new BusinessException(400, "ISBN已存在");
            }
        }
        
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCategory(request.getCategory());
        book.setLocation(request.getLocation());
        book.setTotalQuantity(request.getTotalQuantity());
        book.setAvailableQuantity(request.getTotalQuantity());
        book.setDescription(request.getDescription());
        bookDao.save(book);
        
        log.info("添加书籍成功: {}", book.getTitle());
        return book;
    }

    public Book updateBook(Long id, BookRequest request) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        
        if (request.getIsbn() != null && !request.getIsbn().isEmpty()) {
            if (!request.getIsbn().equals(book.getIsbn()) && bookDao.existsByIsbn(request.getIsbn())) {
                throw new BusinessException(400, "ISBN已存在");
            }
        }
        
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setCategory(request.getCategory());
        book.setLocation(request.getLocation());
        book.setTotalQuantity(request.getTotalQuantity());
        book.setDescription(request.getDescription());
        bookDao.update(book);
        
        log.info("更新书籍成功: {}", book.getTitle());
        return book;
    }

    public void deleteBook(Long id) {
        Book book = bookDao.findById(id);
        if (book == null) {
            throw new BusinessException(404, "书籍不存在");
        }
        bookDao.deleteById(id);
        log.info("删除书籍成功: {}", book.getTitle());
    }
}
