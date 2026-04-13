package com.example.mcp.rest.dto;

import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.OrderRecord;

import java.util.List;
import java.util.Locale;

/**
 * 订单分页响应。
 */
public record OrderPageResponse(
        List<OrderResponse> items,
        int page,
        int size,
        long total,
        String servedBy
) {

    public static OrderPageResponse from(PageResult<OrderRecord> result) {
        return new OrderPageResponse(
                result.items().stream().map(OrderResponse::from).toList(),
                result.page(),
                result.size(),
                result.total(),
                result.servedBy().name().toLowerCase(Locale.ROOT)
        );
    }
}
