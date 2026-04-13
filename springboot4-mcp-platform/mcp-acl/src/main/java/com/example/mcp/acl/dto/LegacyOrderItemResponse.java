package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 旧系统返回的单个订单 DTO。
 */
public record LegacyOrderItemResponse(
        String id,
        @JsonProperty("order_no")
        String orderNo,
        @JsonProperty("customer_name")
        String customerName,
        @JsonProperty("order_status")
        String status,
        @JsonProperty("total_amount")
        BigDecimal amount,
        @JsonProperty("currency_code")
        String currency,
        @JsonProperty("create_time")
        String createdAt
) {
}
