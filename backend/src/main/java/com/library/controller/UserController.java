package com.library.controller;

import com.library.dto.response.ApiResponse;
import com.library.dto.response.PageResponse;
import com.library.exception.BusinessException;
import com.library.model.User;
import com.library.service.UserService;
import com.library.util.JwtUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户信息管理")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户列表（管理员）")
    public ApiResponse<PageResponse<User>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        PageResponse<User> response = userService.getUsers(search, page, pageSize);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户详情")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户")
    public ApiResponse<User> updateUser(@PathVariable Long id,
                                        @RequestBody Map<String, Object> updates,
                                        @AuthenticationPrincipal JwtUserDetails userDetails) {
        Long currentUserId = getUserId(userDetails);
        String currentUserRole = userDetails.getAuthorities().iterator().next().getAuthority();
        User user = userService.updateUser(id, updates, currentUserId, currentUserRole);
        return ApiResponse.success("更新成功", user);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除用户（管理员）")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ApiResponse.success("删除成功");
    }

    private Long getUserId(JwtUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(401, "未登录");
        }
        return userDetails.getUserId();
    }
}
