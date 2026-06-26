package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.model.BorrowRecord;
import com.library.service.BorrowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
@Tag(name = "借阅管理", description = "图书借阅与归还")
public class BorrowController {

    private final BorrowService borrowService;

    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的借阅记录")
    public ApiResponse<PageResponse<BorrowRecord>> getMyBorrowRecords(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            HttpServletRequest request) {
        // TODO: 实现查询逻辑
        return null;
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有借阅记录（管理员）")
    public ApiResponse<PageResponse<BorrowRecord>> getAllBorrowRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现查询逻辑
        return null;
    }

    @PostMapping("/{bookId}")
    @Operation(summary = "借书")
    public ApiResponse<Void> borrowBook(@PathVariable Long bookId,
                                        HttpServletRequest request) {
        // TODO: 实现借书逻辑
        return null;
    }

    @PostMapping("/return/{recordId}")
    @Operation(summary = "还书")
    public ApiResponse<Void> returnBook(@PathVariable Long recordId,
                                        HttpServletRequest request) {
        // TODO: 实现还书逻辑
        return null;
    }
}
