package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 旧系统活跃用户统计响应。
 */
public record LegacyActiveUserCountResponse(
        @JsonProperty("activeUsers")
        long activeUsers
) {
}
