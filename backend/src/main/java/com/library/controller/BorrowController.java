package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.BorrowRecord;
import com.library.service.BorrowService;
import com.library.util.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
@Tag(name = "借阅管理", description = "图书借阅与归还")
public class BorrowController {

    @Autowired
    private BorrowService borrowService;

    @GetMapping("/my")
    @Operation(summary = "获取我的借阅记录")
    public ApiResponse<PageResponse<BorrowRecord>> getMyBorrowRecords(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        PageResponse<BorrowRecord> response = borrowService.getMyBorrowRecords(userId, status, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有借阅记录（管理员）")
    public ApiResponse<PageResponse<BorrowRecord>> getAllBorrowRecords(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<BorrowRecord> response = borrowService.getAllBorrowRecords(status, userId, page, pageSize);
        return ApiResponse.success(response);
    }

    @PostMapping("/{bookId}")
    @Operation(summary = "借书")
    public ApiResponse<Void> borrowBook(@PathVariable Long bookId,
                                        @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        borrowService.borrowBook(userId, bookId);
        return ApiResponse.success("借阅成功");
    }

    @PostMapping("/return/{recordId}")
    @Operation(summary = "还书")
    public ApiResponse<Void> returnBook(@PathVariable Long recordId,
                                        @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long userId = getUserId(userDetails);
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();
        borrowService.returnBook(userId, recordId, userRole);
        return ApiResponse.success("归还成功");
    }

    private Long getUserId(JwtUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(401, "未登录");
        }
        return userDetails.getUserId();
    }
}