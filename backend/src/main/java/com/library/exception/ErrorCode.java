package com.library.exception;

public enum ErrorCode {

    UNAUTHORIZED(401, "未授权访问"),
    FORBIDDEN(403, "权限不足"),
    INVALID_TOKEN(401, "无效的认证令牌"),
    TOKEN_EXPIRED(401, "令牌已过期"),

    USER_NOT_FOUND(404, "用户不存在"),
    USERNAME_EXISTS(400, "用户名已存在"),
    INVALID_CREDENTIALS(401, "用户名或密码错误"),
    USER_DISABLED(403, "用户已被禁用"),

    BOOK_NOT_FOUND(404, "书籍不存在"),
    ISBN_EXISTS(400, "ISBN已存在"),
    BOOK_HAS_BORROWS(400, "该书籍有未归还的借阅记录，无法删除"),

    BORROW_NOT_FOUND(404, "借阅记录不存在"),
    ALREADY_BORROWED(400, "您已经借阅了该书，尚未归还"),
    INSUFFICIENT_STOCK(400, "该书库存不足"),
    ALREADY_RETURNED(400, "该书已经归还"),
    CANNOT_RETURN_OTHERS(400, "无权归还他人书籍"),

    VALIDATION_ERROR(400, "参数校验失败"),
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
}
