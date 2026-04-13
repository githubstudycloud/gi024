package com.example.mcp.domain.exception;

/**
 * 订单不存在时抛出的领域异常。
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String id) {
        super("未找到订单，id=" + id);
    }
}
