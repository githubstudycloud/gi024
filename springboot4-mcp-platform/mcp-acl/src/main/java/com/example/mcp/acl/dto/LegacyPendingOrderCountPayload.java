package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 旧系统待处理订单统计 data 节点。
 */
public record LegacyPendingOrderCountPayload(
        @JsonProperty("pendingCount")
        long pendingCount
) {
}
