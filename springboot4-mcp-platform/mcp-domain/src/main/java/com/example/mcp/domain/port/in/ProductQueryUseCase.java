package com.example.mcp.domain.port.in;

import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.ProductRecord;

/**
 * 商品查询用例。
 */
public interface ProductQueryUseCase {

    PageResult<ProductRecord> search(String query, String category, int page, int size);

    ItemResult<ProductRecord> findById(String id);

    ActiveProductCount countActiveProducts();

    String buildWeeklySummaryPrompt(String week);
}
