package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.model.User;
import com.library.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息管理")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "获取用户列表（管理员）")
    public ApiResponse<PageResponse<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // TODO: 实现查询逻辑
        return null;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public ApiResponse<Map<String, Object>> getUserById(@PathVariable Long id) {
        // TODO: 实现详情逻辑
        return null;
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ApiResponse<User> updateUser(@PathVariable Long id,
                                        @RequestBody Map<String, Object> updates,
                                        HttpServletRequest request) {
        // TODO: 实现更新逻辑
        return null;
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户（管理员）")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        // TODO: 实现删除逻辑
        return null;
    }
}
