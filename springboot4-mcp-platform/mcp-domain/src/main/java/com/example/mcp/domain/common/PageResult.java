package com.example.mcp.domain.common;

import java.util.List;
import java.util.function.Function;

/**
 * 统一的分页返回对象，同时附带当前请求命中的数据源。
 *
 * @param items 数据项
 * @param page 当前页码，从 0 开始
 * @param size 每页大小
 * @param total 总记录数
 * @param servedBy 当前数据源
 * @param <T> 数据类型
 */
public record PageResult<T>(
        List<T> items,
        int page,
        int size,
        long total,
        ServedBy servedBy
) {

    public <R> PageResult<R> map(Function<T, R> mapper) {
        return new PageResult<>(
                items.stream().map(mapper).toList(),
                page,
                size,
                total,
                servedBy
        );
    }
}
