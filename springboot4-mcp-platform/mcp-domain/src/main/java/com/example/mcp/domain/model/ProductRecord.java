package com.example.mcp.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 商品域的统一领域模型。
 *
 * @param id 商品 ID
 * @param productCode 商品编码
 * @param name 商品名称
 * @param category 商品分类
 * @param price 商品价格
 * @param currency 币种
 * @param active 是否启用
 * @param createdAt 创建时间
 */
public record ProductRecord(
        String id,
        String productCode,
        String name,
        String category,
        BigDecimal price,
        String currency,
        boolean active,
        Instant createdAt
) {
}
