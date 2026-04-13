package com.example.mcp.domain.exception;

/**
 * 用户不存在时抛出的领域异常。
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String id) {
        super("未找到用户，id=" + id);
    }
}
