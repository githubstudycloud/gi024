package com.example.mcp.acl.dto;

import java.util.List;

/**
 * 旧系统订单分页响应。
 */
public record LegacyOrderPageResponse(
        List<LegacyOrderItemResponse> data,
        int page,
        int size,
        long total
) {
}
