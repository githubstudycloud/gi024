package com.example.mcp.domain.common;

/**
 * 待处理订单统计结果。
 *
 * @param count 待处理订单数量
 * @param servedBy 当前数据源
 */
public record PendingOrderCount(long count, ServedBy servedBy) {
}
