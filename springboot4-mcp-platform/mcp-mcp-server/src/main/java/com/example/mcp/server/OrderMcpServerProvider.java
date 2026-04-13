package com.example.mcp.server;

import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.PendingOrderCount;
import com.example.mcp.domain.model.OrderRecord;
import com.example.mcp.domain.port.in.OrderQueryUseCase;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * 订单域 MCP 能力提供者。
 */
@Component
public class OrderMcpServerProvider {

    private final OrderQueryUseCase orderQueryUseCase;

    public OrderMcpServerProvider(OrderQueryUseCase orderQueryUseCase) {
        this.orderQueryUseCase = orderQueryUseCase;
    }

    @McpTool(name = "search_orders", description = "按关键字、状态和分页条件搜索订单")
    public SearchOrdersToolResult searchOrders(
            @McpToolParam(description = "检索关键字", required = true) String query,
            @McpToolParam(description = "订单状态过滤", required = false) String status,
            @McpToolParam(description = "页码，从 0 开始", required = false) Integer page,
            @McpToolParam(description = "每页大小，默认 20", required = false) Integer size
    ) {
        PageResult<OrderRecord> result = orderQueryUseCase.search(
                query,
                status,
                page == null ? 0 : page,
                size == null ? 20 : size
        );
        return SearchOrdersToolResult.from(result);
    }

    @McpTool(name = "get_order_detail", description = "根据订单 ID 获取详情")
    public OrderDetailToolResult getOrderDetail(
            @McpToolParam(description = "订单 ID", required = true) String orderId
    ) {
        return OrderDetailToolResult.from(orderQueryUseCase.findById(orderId));
    }

    @McpResource(
            uri = "mcp://orders/pending-count",
            name = "pending-order-count",
            title = "待处理订单数量",
            description = "返回当前待处理订单数量",
            mimeType = "application/json"
    )
    public ReadResourceResult pendingOrderCount() {
        PendingOrderCount result = orderQueryUseCase.countPendingOrders();
        String payload = """
                {
                  "pendingOrders": %d,
                  "servedBy": "%s"
                }
                """.formatted(result.count(), result.servedBy().name().toLowerCase(Locale.ROOT));

        return new ReadResourceResult(List.of(
                new TextResourceContents("mcp://orders/pending-count", "application/json", payload)
        ));
    }

    @McpPrompt(
            name = "weekly_order_summary",
            title = "订单周报提示词",
            description = "生成订单周报分析所需的提示词"
    )
    public GetPromptResult weeklyOrderSummary(
            @McpArg(name = "week", description = "ISO 周，例如 2026-W15", required = true) String week
    ) {
        return new GetPromptResult(
                "订单周报提示词",
                List.of(new PromptMessage(Role.USER, new TextContent(orderQueryUseCase.buildWeeklySummaryPrompt(week))))
        );
    }

    /**
     * MCP Tool 返回的订单列表结果。
     */
    public record SearchOrdersToolResult(
            List<OrderToolView> items,
            int page,
            int size,
            long total,
            String servedBy
    ) {

        static SearchOrdersToolResult from(PageResult<OrderRecord> result) {
            return new SearchOrdersToolResult(
                    result.items().stream().map(OrderToolView::from).toList(),
                    result.page(),
                    result.size(),
                    result.total(),
                    result.servedBy().name().toLowerCase(Locale.ROOT)
            );
        }
    }

    /**
     * MCP Tool 返回的订单详情结果。
     */
    public record OrderDetailToolResult(
            OrderToolView order,
            String servedBy
    ) {

        static OrderDetailToolResult from(ItemResult<OrderRecord> result) {
            return new OrderDetailToolResult(
                    OrderToolView.from(result.data()),
                    result.servedBy().name().toLowerCase(Locale.ROOT)
            );
        }
    }

    /**
     * MCP 对外暴露的订单视图。
     */
    public record OrderToolView(
            String id,
            String orderNo,
            String customerName,
            String status,
            BigDecimal amount,
            String currency,
            String createdAt
    ) {

        static OrderToolView from(OrderRecord record) {
            return new OrderToolView(
                    record.id(),
                    record.orderNo(),
                    record.customerName(),
                    record.status(),
                    record.amount(),
                    record.currency(),
                    record.createdAt().toString()
            );
        }
    }
}
