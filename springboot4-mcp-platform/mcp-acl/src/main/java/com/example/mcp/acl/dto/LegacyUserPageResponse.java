package com.example.mcp.acl.dto;

import java.util.List;

/**
 * 旧系统用户分页响应。
 */
public record LegacyUserPageResponse(
        List<LegacyUserItemResponse> data,
        int page,
        int size,
        long total
) {
}
