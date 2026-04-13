package com.example.mcp.application.service;

import com.example.mcp.application.config.MigrationFlagsProperties;
import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.exception.ProductNotFoundException;
import com.example.mcp.domain.model.ProductRecord;
import com.example.mcp.domain.port.in.ProductQueryUseCase;
import com.example.mcp.domain.port.out.LegacyProductGateway;
import com.example.mcp.domain.port.out.NewProductQueryGateway;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Optional;

/**
 * 商品查询应用服务。
 */
@Service
@Observed(name = "product.query.service")
public class ProductQueryService implements ProductQueryUseCase {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final LegacyProductGateway legacyProductGateway;
    private final NewProductQueryGateway newProductQueryGateway;
    private final MigrationFlagsProperties migrationFlags;

    public ProductQueryService(
            LegacyProductGateway legacyProductGateway,
            NewProductQueryGateway newProductQueryGateway,
            MigrationFlagsProperties migrationFlags
    ) {
        this.legacyProductGateway = legacyProductGateway;
        this.newProductQueryGateway = newProductQueryGateway;
        this.migrationFlags = migrationFlags;
    }

    @Override
    public PageResult<ProductRecord> search(String query, String category, int page, int size) {
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("商品查询关键字不能为空");
        }
        if (page < 0) {
            throw new IllegalArgumentException("页码不能小于 0");
        }

        int normalizedSize = normalizeSize(size);
        if (migrationFlags.isProductSourceNew()) {
            return newProductQueryGateway.search(query.trim(), trimToNull(category), page, normalizedSize);
        }
        return legacyProductGateway.search(query.trim(), trimToNull(category), page, normalizedSize);
    }

    @Override
    public ItemResult<ProductRecord> findById(String id) {
        if (!StringUtils.hasText(id)) {
            throw new IllegalArgumentException("商品 ID 不能为空");
        }

        return currentGateway().findById(id.trim())
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public ActiveProductCount countActiveProducts() {
        return currentGateway().countActiveProducts();
    }

    @Override
    public String buildWeeklySummaryPrompt(String week) {
        if (!StringUtils.hasText(week)) {
            throw new IllegalArgumentException("统计周不能为空");
        }

        ActiveProductCount activeProductCount = countActiveProducts();
        String source = activeProductCount.servedBy().name().toLowerCase(Locale.ROOT);
        return """
                你正在生成商品周报，请遵循以下要求：
                1. 统计周：%s
                2. 当前启用商品数：%d
                3. 当前数据来源：%s
                4. 请优先使用 MCP Tool：search_products、get_product_detail
                5. 请结合 MCP Resource：mcp://products/active-count
                6. 输出需要覆盖分类分布、价格带变化、上新节奏和停用风险
                """.formatted(week.trim(), activeProductCount.count(), source);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private LegacyOrNewGateway currentGateway() {
        return migrationFlags.isProductSourceNew()
                ? new LegacyOrNewGateway(newProductQueryGateway)
                : new LegacyOrNewGateway(legacyProductGateway);
    }

    /**
     * 使用薄包装统一新旧商品网关访问。
     */
    private record LegacyOrNewGateway(Object gateway) {

        Optional<ItemResult<ProductRecord>> findById(String id) {
            if (gateway instanceof NewProductQueryGateway newGateway) {
                return newGateway.findById(id);
            }
            return ((LegacyProductGateway) gateway).findById(id);
        }

        ActiveProductCount countActiveProducts() {
            if (gateway instanceof NewProductQueryGateway newGateway) {
                return newGateway.countActiveProducts();
            }
            return ((LegacyProductGateway) gateway).countActiveProducts();
        }
    }
}
