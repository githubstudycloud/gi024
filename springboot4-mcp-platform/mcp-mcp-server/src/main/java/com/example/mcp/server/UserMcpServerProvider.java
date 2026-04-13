package com.example.mcp.server;

import com.example.mcp.domain.common.ActiveUserCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.UserProfile;
import com.example.mcp.domain.port.in.UserQueryUseCase;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * 用户域 MCP 能力提供者。
 */
@Component
public class UserMcpServerProvider {

    private final UserQueryUseCase userQueryUseCase;

    public UserMcpServerProvider(UserQueryUseCase userQueryUseCase) {
        this.userQueryUseCase = userQueryUseCase;
    }

    @McpTool(name = "search_users", description = "按关键字、角色和分页条件搜索用户")
    public SearchUsersToolResult searchUsers(
            @McpToolParam(description = "检索关键字", required = true) String query,
            @McpToolParam(description = "角色过滤", required = false) String role,
            @McpToolParam(description = "页码，从 0 开始", required = false) Integer page,
            @McpToolParam(description = "每页大小，默认 20", required = false) Integer size
    ) {
        PageResult<UserProfile> result = userQueryUseCase.search(
                query,
                role,
                page == null ? 0 : page,
                size == null ? 20 : size
        );
        return SearchUsersToolResult.from(result);
    }

    @McpTool(name = "get_user_detail", description = "根据用户 ID 获取详情")
    public UserDetailToolResult getUserDetail(
            @McpToolParam(description = "用户 ID", required = true) String userId
    ) {
        return UserDetailToolResult.from(userQueryUseCase.findById(userId));
    }

    @McpResource(
            uri = "mcp://users/active-count",
            name = "active-user-count",
            title = "活跃用户数量",
            description = "返回当前活跃用户数量",
            mimeType = "application/json"
    )
    public ReadResourceResult activeUserCount() {
        ActiveUserCount result = userQueryUseCase.countActiveUsers();
        String payload = """
                {
                  "activeUsers": %d,
                  "servedBy": "%s"
                }
                """.formatted(result.count(), result.servedBy().name().toLowerCase(Locale.ROOT));

        return new ReadResourceResult(List.of(
                new TextResourceContents("mcp://users/active-count", "application/json", payload)
        ));
    }

    @McpPrompt(
            name = "weekly_user_summary",
            title = "用户周报提示词",
            description = "生成用户周报分析所需的提示词"
    )
    public GetPromptResult weeklyUserSummary(
            @McpArg(name = "week", description = "ISO 周，例如 2026-W15", required = true) String week
    ) {
        return new GetPromptResult(
                "用户周报提示词",
                List.of(new PromptMessage(Role.USER, new TextContent(userQueryUseCase.buildWeeklySummaryPrompt(week))))
        );
    }

    /**
     * MCP Tool 返回的列表结果。
     */
    public record SearchUsersToolResult(
            List<UserToolView> items,
            int page,
            int size,
            long total,
            String servedBy
    ) {

        static SearchUsersToolResult from(PageResult<UserProfile> result) {
            return new SearchUsersToolResult(
                    result.items().stream().map(UserToolView::from).toList(),
                    result.page(),
                    result.size(),
                    result.total(),
                    result.servedBy().name().toLowerCase(Locale.ROOT)
            );
        }
    }

    /**
     * MCP Tool 返回的单用户详情结果。
     */
    public record UserDetailToolResult(
            UserToolView user,
            String servedBy
    ) {

        static UserDetailToolResult from(ItemResult<UserProfile> result) {
            return new UserDetailToolResult(
                    UserToolView.from(result.data()),
                    result.servedBy().name().toLowerCase(Locale.ROOT)
            );
        }
    }

    /**
     * MCP 对外暴露的用户视图。
     */
    public record UserToolView(
            String id,
            String username,
            String displayName,
            String email,
            String role,
            boolean active,
            String createdAt
    ) {

        static UserToolView from(UserProfile profile) {
            return new UserToolView(
                    profile.id(),
                    profile.username(),
                    profile.displayName(),
                    profile.email(),
                    profile.role(),
                    profile.active(),
                    profile.createdAt().toString()
            );
        }
    }
}
