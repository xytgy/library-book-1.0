package com.library.controller;

import com.library.dto.request.BookRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.model.Book;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "书籍管理", description = "书籍的增删改查")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = "获取书籍列表")
    public ApiResponse<PageResponse<Book>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现查询逻辑
        return null;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取书籍详情")
    public ApiResponse<Book> getBookById(@PathVariable Long id) {
        // TODO: 实现详情逻辑
        return null;
    }

    @PostMapping
    @Operation(summary = "添加书籍（管理员）")
    public ApiResponse<Book> createBook(@Valid @RequestBody BookRequest request) {
        // TODO: 实现添加逻辑
        return null;
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新书籍（管理员）")
    public ApiResponse<Book> updateBook(@PathVariable Long id,
                                        @Valid @RequestBody BookRequest request) {
        // TODO: 实现更新逻辑
        return null;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除书籍（管理员）")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        // TODO: 实现删除逻辑
        return null;
    }
}
