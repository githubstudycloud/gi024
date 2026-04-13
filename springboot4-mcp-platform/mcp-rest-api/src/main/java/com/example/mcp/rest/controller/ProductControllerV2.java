package com.example.mcp.rest.controller;

import com.example.mcp.domain.port.in.ProductQueryUseCase;
import com.example.mcp.rest.dto.ProductDetailResponse;
import com.example.mcp.rest.dto.ProductPageResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品域 v2 REST 接口。
 */
@Validated
@RestController
@RequestMapping("/api/v2/products")
public class ProductControllerV2 {

    private final ProductQueryUseCase productQueryUseCase;

    public ProductControllerV2(ProductQueryUseCase productQueryUseCase) {
        this.productQueryUseCase = productQueryUseCase;
    }

    @GetMapping
    public ProductPageResponse search(
            @RequestParam @NotBlank(message = "商品查询关键字不能为空") String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "页码不能小于 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页大小必须大于 0") int size
    ) {
        return ProductPageResponse.from(productQueryUseCase.search(query, category, page, size));
    }

    @GetMapping("/{id}")
    public ProductDetailResponse getById(@PathVariable String id) {
        return ProductDetailResponse.from(productQueryUseCase.findById(id));
    }
}
