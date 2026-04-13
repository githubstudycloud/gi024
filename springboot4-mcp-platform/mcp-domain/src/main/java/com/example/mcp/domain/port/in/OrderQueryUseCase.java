package com.example.mcp.domain.port.in;

import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.PendingOrderCount;
import com.example.mcp.domain.model.OrderRecord;

/**
 * 订单查询用例，供 REST 与 MCP 入口统一复用。
 */
public interface OrderQueryUseCase {

    PageResult<OrderRecord> search(String query, String status, int page, int size);

    ItemResult<OrderRecord> findById(String id);

    PendingOrderCount countPendingOrders();

    String buildWeeklySummaryPrompt(String week);
}
