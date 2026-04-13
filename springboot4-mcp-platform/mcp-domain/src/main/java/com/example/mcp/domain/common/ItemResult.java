package com.example.mcp.domain.common;

/**
 * 单对象查询结果，同时标记数据来源。
 *
 * @param data 查询结果
 * @param servedBy 当前数据源
 * @param <T> 数据类型
 */
public record ItemResult<T>(T data, ServedBy servedBy) {
}
