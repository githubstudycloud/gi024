package com.example.mcp.rest.dto;

import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.ProductRecord;

import java.util.List;
import java.util.Locale;

/**
 * 商品分页响应。
 */
public record ProductPageResponse(
        List<ProductResponse> items,
        int page,
        int size,
        long total,
        String servedBy
) {

    public static ProductPageResponse from(PageResult<ProductRecord> result) {
        return new ProductPageResponse(
                result.items().stream().map(ProductResponse::from).toList(),
                result.page(),
                result.size(),
                result.total(),
                result.servedBy().name().toLowerCase(Locale.ROOT)
        );
    }
}
