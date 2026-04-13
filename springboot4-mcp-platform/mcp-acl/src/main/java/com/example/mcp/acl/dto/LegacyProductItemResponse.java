package com.example.mcp.acl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * 旧系统返回的单个商品 DTO。
 */
public record LegacyProductItemResponse(
        @JsonProperty("product_id")
        String id,
        @JsonProperty("product_code")
        String productCode,
        @JsonProperty("product_name")
        String name,
        @JsonProperty("category_name")
        String category,
        @JsonProperty("sale_price")
        BigDecimal price,
        @JsonProperty("currency")
        String currency,
        @JsonProperty("enabled")
        boolean active,
        @JsonProperty("gmt_create")
        String createdAt
) {
}
