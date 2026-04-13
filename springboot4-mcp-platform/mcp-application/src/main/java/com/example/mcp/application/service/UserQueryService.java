package com.example.mcp.application.service;

import com.example.mcp.application.config.MigrationFlagsProperties;
import com.example.mcp.domain.common.ActiveUserCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.exception.UserNotFoundException;
import com.example.mcp.domain.model.UserProfile;
import com.example.mcp.domain.port.in.UserQueryUseCase;
import com.example.mcp.domain.port.out.LegacyUserGateway;
import com.example.mcp.domain.port.out.NewUserQueryGateway;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

/**
 * 用户查询应用服务。
 *
 * <p>该服务是新旧数据源切换的唯一入口，REST 与 MCP 只依赖它。</p>
 */
@Service
@Observed(name = "user.query.service")
public class UserQueryService implements UserQueryUseCase {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final LegacyUserGateway legacyUserGateway;
    private final NewUserQueryGateway newUserQueryGateway;
    private final MigrationFlagsProperties migrationFlags;

    public UserQueryService(
            LegacyUserGateway legacyUserGateway,
            NewUserQueryGateway newUserQueryGateway,
            MigrationFlagsProperties migrationFlags
    ) {
        this.legacyUserGateway = legacyUserGateway;
        this.newUserQueryGateway = newUserQueryGateway;
        this.migrationFlags = migrationFlags;
    }

    @Override
    public PageResult<UserProfile> search(String query, String role, int page, int size) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("查询关键字不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于 0");
        }

        int normalizedSize = normalizeSize(size);
        if (migrationFlags.isUserSourceNew()) {
            return newUserQueryGateway.search(query.trim(), trimToNull(role), page, normalizedSize);
        }
        return legacyUserGateway.search(query.trim(), trimToNull(role), page, normalizedSize);
    }

    @Override
    public ItemResult<UserProfile> findById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("用户 ID 不能为空");
        }

        return currentGateway().findById(id.trim())
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public ActiveUserCount countActiveUsers() {
        return currentGateway().countActiveUsers();
    }

    @Override
    public String buildWeeklySummaryPrompt(String week) {
        if (!StringUtils.hasText(week)) {
            throw new IllegalArgumentException("统计周不能为空");
        }

        ActiveUserCount activeUserCount = countActiveUsers();
        String source = activeUserCount.servedBy().name().toLowerCase(Locale.ROOT);
        return """
                你正在生成用户周报，请遵循以下要求：
                1. 统计周：%s
                2. 当前活跃用户数：%d
                3. 当前数据来源：%s
                4. 请优先使用 MCP Tool：search_users、get_user_detail
                5. 请结合 MCP Resource：mcp://users/active-count
                6. 输出需要覆盖活跃度、角色分布、异常趋势和后续风险
                """.formatted(week.trim(), activeUserCount.count(), source);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private LegacyOrNewGateway currentGateway() {
        return migrationFlags.isUserSourceNew()
                ? new LegacyOrNewGateway(newUserQueryGateway)
                : new LegacyOrNewGateway(legacyUserGateway);
    }

    /**
     * 用一个很薄的包装避免把三段重复 if/else 分散到每个方法里。
     */
    private record LegacyOrNewGateway(Object gateway) {

        Optional<ItemResult<UserProfile>> findById(String id) {
            if (gateway instanceof NewUserQueryGateway newGateway) {
                return newGateway.findById(id);
            }
            return ((LegacyUserGateway) gateway).findById(id);
        }

        ActiveUserCount countActiveUsers() {
            if (gateway instanceof NewUserQueryGateway newGateway) {
                return newGateway.countActiveUsers();
            }
            return ((LegacyUserGateway) gateway).countActiveUsers();
        }
    }
}
