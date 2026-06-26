package com.library.service;

import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.exception.ErrorCode;
import com.library.model.BorrowRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowService {

    private static final Logger log = LoggerFactory.getLogger(BorrowService.class);

    private final JdbcTemplate jdbcTemplate;

    public BorrowService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public PageResponse<BorrowRecord> getMyBorrowRecords(Long userId, String status, int page, int pageSize) {
        // TODO: 实现查询逻辑
        // 1. 构建查询 SQL（JOIN books 表获取书名、作者）
        // 2. 执行查询
        // 3. 返回 PageResponse
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }

    public PageResponse<BorrowRecord> getAllBorrowRecords(String status, Long userId, int page, int pageSize) {
        // TODO: 实现查询逻辑（管理员）
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }

    @Transactional
    public void borrowBook(Long userId, Long bookId) {
        // TODO: 实现借书逻辑
        // 1. 检查书籍是否存在
        // 2. 检查库存是否充足
        // 3. 检查用户是否已借阅该书（未归还）
        // 4. 创建借阅记录（借期30天）
        // 5. 扣减库存
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }

    @Transactional
    public void returnBook(Long userId, Long recordId, String userRole) {
        // TODO: 实现还书逻辑
        // 1. 检查借阅记录是否存在
        // 2. 检查是否已归还
        // 3. 检查是否有权归还（本人或管理员）
        // 4. 更新借阅记录状态
        // 5. 增加库存
        throw new BusinessException(ErrorCode.INTERNAL_ERROR);
    }
}
