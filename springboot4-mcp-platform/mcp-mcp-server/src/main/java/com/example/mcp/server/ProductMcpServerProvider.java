package com.example.mcp.server;

import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.model.ProductRecord;
import com.example.mcp.domain.port.in.ProductQueryUseCase;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.springframework.ai.mcp.annotation.McpArg;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * 商品域 MCP 能力提供者。
 */
@Component
public class ProductMcpServerProvider {

    private final ProductQueryUseCase productQueryUseCase;

    public ProductMcpServerProvider(ProductQueryUseCase productQueryUseCase) {
        this.productQueryUseCase = productQueryUseCase;
    }

    @McpTool(name = "search_products", description = "按关键字、分类和分页条件搜索商品")
    public SearchProductsToolResult searchProducts(
            @McpToolParam(description = "检索关键字", required = true) String query,
            @McpToolParam(description = "商品分类过滤", required = false) String category,
            @McpToolParam(description = "页码，从 0 开始", required = false) Integer page,
            @McpToolParam(description = "每页大小，默认 20", required = false) Integer size
    ) {
        PageResult<ProductRecord> result = productQueryUseCase.search(
                query,
                category,
                page == null ? 0 : page,
                size == null ? 20 : size
        );
        return SearchProductsToolResult.from(result);
    }

    @McpTool(name = "get_product_detail", description = "根据商品 ID 获取详情")
    public ProductDetailToolResult getProductDetail(
            @McpToolParam(description = "商品 ID", required = true) String productId
    ) {
        return ProductDetailToolResult.from(productQueryUseCase.findById(productId));
    }

    @McpResource(
            uri = "mcp://products/active-count",
            name = "active-product-count",
            title = "启用商品数量",
            description = "返回当前启用商品数量",
            mimeType = "application/json"
    )
    public ReadResourceResult activeProductCount() {
        ActiveProductCount result = productQueryUseCase.countActiveProducts();
        String payload = """
                {
                  "activeProducts": %d,
                  "servedBy": "%s"
                }
                """.formatted(result.count(), result.servedBy().name().toLowerCase(Locale.ROOT));

        return new ReadResourceResult(List.of(
                new TextResourceContents("mcp://products/active-count", "application/json", payload)
        ));
    }

    @McpPrompt(
            name = "weekly_product_summary",
            title = "商品周报提示词",
            description = "生成商品周报分析所需的提示词"
    )
    public GetPromptResult weeklyProductSummary(
            @McpArg(name = "week", description = "ISO 周，例如 2026-W15", required = true) String week
    ) {
        return new GetPromptResult(
                "商品周报提示词",
                List.of(new PromptMessage(Role.USER, new TextContent(productQueryUseCase.buildWeeklySummaryPrompt(week))))
        );
    }

    /**
     * MCP Tool 返回的商品列表结果。
     */
    public record SearchProductsToolResult(
            List<ProductToolView> items,
            int page,
            int size,
            long total,
            String servedBy
    ) {

        static SearchProductsToolResult from(PageResult<ProductRecord> result) {
            return new SearchProductsToolResult(
                    result.items().stream().map(ProductToolView::from).toList(),
                    result.page(),
                    result.size(),
                    result.total(),
                    result.servedBy().name().toLowerCase(Locale.ROOT)
            );
        }
    }

    /**
     * MCP Tool 返回的商品详情结果。
     */
    public record ProductDetailToolResult(
            ProductToolView product,
            String servedBy
    ) {

        static ProductDetailToolResult from(ItemResult<ProductRecord> result) {
            return new ProductDetailToolResult(
                    ProductToolView.from(result.data()),
                    result.servedBy().name().toLowerCase(Locale.ROOT)
            );
        }
    }

    /**
     * MCP 对外暴露的商品视图。
     */
    public record ProductToolView(
            String id,
            String productCode,
            String name,
            String category,
            BigDecimal price,
            String currency,
            boolean active,
            String createdAt
    ) {

        static ProductToolView from(ProductRecord record) {
            return new ProductToolView(
                    record.id(),
                    record.productCode(),
                    record.name(),
                    record.category(),
                    record.price(),
                    record.currency(),
                    record.active(),
                    record.createdAt().toString()
            );
        }
    }
}
