package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 旧系统待处理订单统计响应。
 */
public record LegacyPendingOrderCountResponse(
        @JsonProperty("pendingOrders")
        long pendingOrders
) {
}
