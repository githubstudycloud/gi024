package com.example.mcp.rest.dto;

import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.model.ProductRecord;

import java.util.Locale;

/**
 * 商品详情响应。
 */
public record ProductDetailResponse(
        ProductResponse data,
        String servedBy
) {

    public static ProductDetailResponse from(ItemResult<ProductRecord> result) {
        return new ProductDetailResponse(
                ProductResponse.from(result.data()),
                result.servedBy().name().toLowerCase(Locale.ROOT)
        );
    }
}
