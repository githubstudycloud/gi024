package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 旧系统返回的单个用户 DTO。
 */
public record LegacyUserItemResponse(
        String id,
        @JsonProperty("user_name")
        String username,
        @JsonProperty("display_name")
        String displayName,
        String email,
        String role,
        @JsonProperty("is_active")
        boolean active,
        @JsonProperty("create_time")
        String createdAt
) {
}
