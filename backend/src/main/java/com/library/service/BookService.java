package com.library.service;

import com.library.dto.request.BookRequest;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import com.library.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final JdbcTemplate jdbcTemplate;

    public BookService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<Book> getBooks(String search, String category, int page, int pageSize) {
        // TODO: 实现查询逻辑
        // 1. 构建查询 SQL（带条件和分页）
        // 2. 执行查询
        // 3. 查询总数
        // 4. 返回 PageResponse
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }

    public Book getBookById(Long id) {
        // TODO: 实现详情逻辑
        throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
    }

    @Transactional
    public Book createBook(BookRequest request) {
        // TODO: 实现添加逻辑
        // 1. 检查 ISBN 唯一性
        // 2. 插入书籍记录
        // 3. 返回新书籍
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }

    @Transactional
    public Book updateBook(Long id, BookRequest request) {
        // TODO: 实现更新逻辑
        // 1. 检查书籍是否存在
        // 2. 检查 ISBN 唯一性（排除当前记录）
        // 3. 更新书籍记录
        // 4. 返回更新后的书籍
        throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
    }

    @Transactional
    public void deleteBook(Long id) {
        // TODO: 实现删除逻辑
        // 1. 检查书籍是否存在
        // 2. 检查是否有未归还的借阅记录
        // 3. 删除书籍
        throw new BusinessException(ErrorCode.BOOK_NOT_FOUND);
    }
}
