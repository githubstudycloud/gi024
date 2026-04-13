package com.example.mcp.rest.dto;

import com.example.mcp.domain.model.ProductRecord;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 对外返回的商品 DTO。
 */
public record ProductResponse(
        String id,
        String productCode,
        String name,
        String category,
        BigDecimal price,
        String currency,
        boolean active,
        Instant createdAt
) {

    public static ProductResponse from(ProductRecord record) {
        return new ProductResponse(
                record.id(),
                record.productCode(),
                record.name(),
                record.category(),
                record.price(),
                record.currency(),
                record.active(),
                record.createdAt()
        );
    }
}
