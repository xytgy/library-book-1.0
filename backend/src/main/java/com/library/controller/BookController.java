package com.library.controller;

import com.library.dto.request.BookRequest;
import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.model.Book;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@Tag(name = "书籍管理", description = "书籍的增删改查")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    @Operation(summary = "获取书籍列表")
    public ApiResponse<PageResponse<Book>> getBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<Book> response = bookService.getBooks(search, category, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取书籍详情")
    public ApiResponse<Book> getBookById(@PathVariable Long id) {
        Book book = bookService.getBookById(id);
        return ApiResponse.success(book);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "添加书籍（管理员）")
    public ApiResponse<Book> createBook(@Valid @RequestBody BookRequest request) {
        Book book = bookService.createBook(request);
        return ApiResponse.success("添加成功", book);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新书籍（管理员）")
    public ApiResponse<Book> updateBook(@PathVariable Long id,
                                        @Valid @RequestBody BookRequest request) {
        Book book = bookService.updateBook(id, request);
        return ApiResponse.success("更新成功", book);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除书籍（管理员）")
    public ApiResponse<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ApiResponse.success("删除成功");
    }
}
