package com.example.mcp.domain.common;

/**
 * 启用商品统计结果。
 *
 * @param count 启用商品数量
 * @param servedBy 当前数据源
 */
public record ActiveProductCount(long count, ServedBy servedBy) {
}
