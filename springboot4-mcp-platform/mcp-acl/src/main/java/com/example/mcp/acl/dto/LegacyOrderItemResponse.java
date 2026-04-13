package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 旧系统返回的单个订单 DTO。
 */
public record LegacyOrderItemResponse(
        @JsonProperty("order_id")
        String id,
        @JsonProperty("order_code")
        String orderNo,
        @JsonProperty("customer_name")
        String customerName,
        @JsonProperty("status")
        String status,
        @JsonProperty("amount")
        BigDecimal amount,
        @JsonProperty("currency")
        String currency,
        @JsonProperty("gmt_create")
        String createdAt
) {
}
