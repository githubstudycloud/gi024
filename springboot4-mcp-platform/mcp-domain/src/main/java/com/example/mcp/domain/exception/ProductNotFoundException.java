package com.example.mcp.domain.exception;

/**
 * 商品不存在时抛出的领域异常。
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String id) {
        super("未找到商品，id=" + id);
    }
}
