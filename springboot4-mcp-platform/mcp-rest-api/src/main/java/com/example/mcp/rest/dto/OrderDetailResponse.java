package com.example.mcp.rest.dto;

import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.model.OrderRecord;

import java.util.Locale;

/**
 * 订单详情响应。
 */
public record OrderDetailResponse(
        OrderResponse data,
        String servedBy
) {

    public static OrderDetailResponse from(ItemResult<OrderRecord> result) {
        return new OrderDetailResponse(
                OrderResponse.from(result.data()),
                result.servedBy().name().toLowerCase(Locale.ROOT)
        );
    }
}
