package com.example.mcp.domain.port.out;

import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.PendingOrderCount;
import com.example.mcp.domain.model.OrderRecord;

import java.util.Optional;

/**
 * 旧系统订单读接口。
 */
public interface LegacyOrderGateway {

    PageResult<OrderRecord> search(String query, String status, int page, int size);

    Optional<ItemResult<OrderRecord>> findById(String id);

    PendingOrderCount countPendingOrders();
}
