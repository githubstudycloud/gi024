package com.example.mcp.domain.port.in;

import com.example.mcp.domain.common.ActiveUserCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.UserProfile;

/**
 * 用户查询用例，供 REST 与 MCP 入口统一复用。
 */
public interface UserQueryUseCase {

    PageResult<UserProfile> search(String query, String role, int page, int size);

    ItemResult<UserProfile> findById(String id);

    ActiveUserCount countActiveUsers();

    String buildWeeklySummaryPrompt(String week);
}
