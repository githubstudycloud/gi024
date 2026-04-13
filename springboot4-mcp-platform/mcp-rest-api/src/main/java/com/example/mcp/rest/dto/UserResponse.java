package com.example.mcp.rest.dto;

import com.example.mcp.domain.model.UserProfile;

import java.time.Instant;

/**
 * 对外返回的用户 DTO。
 */
public record UserResponse(
        String id,
        String username,
        String displayName,
        String email,
        String role,
        boolean active,
        Instant createdAt
) {

    public static UserResponse from(UserProfile profile) {
        return new UserResponse(
                profile.id(),
                profile.username(),
                profile.displayName(),
                profile.email(),
                profile.role(),
                profile.active(),
                profile.createdAt()
        );
    }
}
