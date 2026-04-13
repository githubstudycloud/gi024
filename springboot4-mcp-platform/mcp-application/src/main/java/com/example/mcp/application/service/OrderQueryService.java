package com.example.mcp.application.service;

import com.example.mcp.application.config.MigrationFlagsProperties;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.PendingOrderCount;
import com.example.mcp.domain.exception.OrderNotFoundException;
import com.example.mcp.domain.model.OrderRecord;
import com.example.mcp.domain.port.in.OrderQueryUseCase;
import com.example.mcp.domain.port.out.LegacyOrderGateway;
import com.example.mcp.domain.port.out.NewOrderQueryGateway;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

/**
 * 订单查询应用服务。
 */
@Service
@Observed(name = "order.query.service")
public class OrderQueryService implements OrderQueryUseCase {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final LegacyOrderGateway legacyOrderGateway;
    private final NewOrderQueryGateway newOrderQueryGateway;
    private final MigrationFlagsProperties migrationFlags;

    public OrderQueryService(
            LegacyOrderGateway legacyOrderGateway,
            NewOrderQueryGateway newOrderQueryGateway,
            MigrationFlagsProperties migrationFlags
    ) {
        this.legacyOrderGateway = legacyOrderGateway;
        this.newOrderQueryGateway = newOrderQueryGateway;
        this.migrationFlags = migrationFlags;
    }

    @Override
    public PageResult<OrderRecord> search(String query, String status, int page, int size) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("订单查询关键字不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于 0");
        }

        int normalizedSize = normalizeSize(size);
        if (migrationFlags.isOrderSourceNew()) {
            return newOrderQueryGateway.search(query.trim(), trimToNull(status), page, normalizedSize);
        }
        return legacyOrderGateway.search(query.trim(), trimToNull(status), page, normalizedSize);
    }

    @Override
    public ItemResult<OrderRecord> findById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("订单 ID 不能为空");
        }

        return currentGateway().findById(id.trim())
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    public PendingOrderCount countPendingOrders() {
        return currentGateway().countPendingOrders();
    }

    @Override
    public String buildWeeklySummaryPrompt(String week) {
        if (!StringUtils.hasText(week)) {
            throw new IllegalArgumentException("统计周不能为空");
        }

        PendingOrderCount pendingOrderCount = countPendingOrders();
        String source = pendingOrderCount.servedBy().name().toLowerCase(Locale.ROOT);
        return """
                你正在生成订单周报，请遵循以下要求：
                1. 统计周：%s
                2. 当前待处理订单数：%d
                3. 当前数据来源：%s
                4. 请优先使用 MCP Tool：search_orders、get_order_detail
                5. 请结合 MCP Resource：mcp://orders/pending-count
                6. 输出需要覆盖订单状态分布、金额波动、积压风险和处理建议
                """.formatted(week.trim(), pendingOrderCount.count(), source);
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
        return migrationFlags.isOrderSourceNew()
                ? new LegacyOrNewGateway(newOrderQueryGateway)
                : new LegacyOrNewGateway(legacyOrderGateway);
    }

    /**
     * 使用薄包装统一新旧订单网关访问。
     */
    private record LegacyOrNewGateway(Object gateway) {

        Optional<ItemResult<OrderRecord>> findById(String id) {
            if (gateway instanceof NewOrderQueryGateway newGateway) {
                return newGateway.findById(id);
            }
            return ((LegacyOrderGateway) gateway).findById(id);
        }

        PendingOrderCount countPendingOrders() {
            if (gateway instanceof NewOrderQueryGateway newGateway) {
                return newGateway.countPendingOrders();
            }
            return ((LegacyOrderGateway) gateway).countPendingOrders();
        }
    }
}
