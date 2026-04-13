package com.example.mcp.rest.dto;

import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.UserProfile;

import java.util.List;
import java.util.Locale;

/**
 * 用户分页响应。
 */
public record UserPageResponse(
        List<UserResponse> items,
        int page,
        int size,
        long total,
        String servedBy
) {

    public static UserPageResponse from(PageResult<UserProfile> result) {
        return new UserPageResponse(
                result.items().stream().map(UserResponse::from).toList(),
                result.page(),
                result.size(),
                result.total(),
                result.servedBy().name().toLowerCase(Locale.ROOT)
        );
    }
}
