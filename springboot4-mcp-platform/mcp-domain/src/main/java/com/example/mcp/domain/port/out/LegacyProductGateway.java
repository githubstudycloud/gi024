package com.example.mcp.domain.port.out;

import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.ProductRecord;

import java.util.Optional;

/**
 * 旧系统商品读接口。
 */
public interface LegacyProductGateway {

    PageResult<ProductRecord> search(String query, String category, int page, int size);

    Optional<ItemResult<ProductRecord>> findById(String id);

    ActiveProductCount countActiveProducts();
}
