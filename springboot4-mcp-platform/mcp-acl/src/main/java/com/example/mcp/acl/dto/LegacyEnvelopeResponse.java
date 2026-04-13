package com.example.mcp.acl.dto;

/**
 * 旧系统常见的统一信封响应。
 *
 * @param code 业务状态码
 * @param message 响应消息
 * @param data 实际数据
 * @param <T> 数据类型
 */
public record LegacyEnvelopeResponse<T>(
        String code,
        String message,
        T data
) {
}
