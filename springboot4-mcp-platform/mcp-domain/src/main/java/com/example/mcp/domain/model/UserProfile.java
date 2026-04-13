package com.example.mcp.domain.model;

import java.time.Instant;

/**
 * 用户域的统一领域模型。
 *
 * @param id 用户 ID
 * @param username 登录名
 * @param displayName 展示名
 * @param email 邮箱
 * @param role 角色
 * @param active 是否启用
 * @param createdAt 创建时间
 */
public record UserProfile(
        String id,
        String username,
        String displayName,
        String email,
        String role,
        boolean active,
        Instant createdAt
) {
}
