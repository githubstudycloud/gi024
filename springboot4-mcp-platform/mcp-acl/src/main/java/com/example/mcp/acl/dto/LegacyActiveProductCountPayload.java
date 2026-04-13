package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 旧系统启用商品统计 data 节点。
 */
public record LegacyActiveProductCountPayload(
        @JsonProperty("activeCount")
        long activeCount
) {
}
