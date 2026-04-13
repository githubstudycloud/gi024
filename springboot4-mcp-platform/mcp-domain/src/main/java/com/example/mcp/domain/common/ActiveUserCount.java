package com.example.mcp.domain.common;

/**
 * 活跃用户统计结果。
 *
 * @param count 活跃用户数量
 * @param servedBy 当前数据源
 */
public record ActiveUserCount(long count, ServedBy servedBy) {
}
