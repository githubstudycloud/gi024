package com.example.mcp.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单域的统一领域模型。
 *
 * @param id 订单 ID
 * @param orderNo 订单号
 * @param customerName 客户名称
 * @param status 订单状态
 * @param amount 订单金额
 * @param currency 币种
 * @param createdAt 创建时间
 */
public record OrderRecord(
        String id,
        String orderNo,
        String customerName,
        String status,
        BigDecimal amount,
        String currency,
        Instant createdAt
) {
}
