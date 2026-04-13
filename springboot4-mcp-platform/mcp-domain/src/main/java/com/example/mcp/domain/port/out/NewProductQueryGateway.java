package com.example.mcp.domain.port.out;

import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.ProductRecord;

import java.util.Optional;

/**
 * 新系统商品查询读接口。
 */
public interface NewProductQueryGateway {

    PageResult<ProductRecord> search(String query, String category, int page, int size);

    Optional<ItemResult<ProductRecord>> findById(String id);

    ActiveProductCount countActiveProducts();
}
