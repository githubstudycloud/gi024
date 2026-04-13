package com.example.mcp.rest.dto;

import com.example.mcp.domain.model.OrderRecord;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 对外返回的订单 DTO。
 */
public record OrderResponse(
        String id,
        String orderNo,
        String customerName,
        String status,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {

    public static OrderResponse from(OrderRecord record) {
        return new OrderResponse(
                record.id(),
                record.orderNo(),
                record.customerName(),
                record.status(),
                record.amount(),
                record.currency(),
                record.createdAt()
        );
    }
}
