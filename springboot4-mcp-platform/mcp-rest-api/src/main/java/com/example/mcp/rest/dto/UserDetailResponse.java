package com.example.mcp.rest.dto;

import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.model.UserProfile;

import java.util.Locale;

/**
 * 用户详情响应。
 */
public record UserDetailResponse(
        UserResponse data,
        String servedBy
) {

    public static UserDetailResponse from(ItemResult<UserProfile> result) {
        return new UserDetailResponse(
                UserResponse.from(result.data()),
                result.servedBy().name().toLowerCase(Locale.ROOT)
        );
    }
}
