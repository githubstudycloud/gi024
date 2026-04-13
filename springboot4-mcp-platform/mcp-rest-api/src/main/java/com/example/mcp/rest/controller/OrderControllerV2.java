package com.example.mcp.rest.controller;

import com.example.mcp.domain.port.in.OrderQueryUseCase;
import com.example.mcp.rest.dto.OrderDetailResponse;
import com.example.mcp.rest.dto.OrderPageResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单域 v2 REST 接口。
 */
@Validated
@RestController
@RequestMapping("/api/v2/orders")
public class OrderControllerV2 {

    private final OrderQueryUseCase orderQueryUseCase;

    public OrderControllerV2(OrderQueryUseCase orderQueryUseCase) {
        this.orderQueryUseCase = orderQueryUseCase;
    }

    @GetMapping
    public OrderPageResponse search(
            @RequestParam @NotBlank(message = "订单查询关键字不能为空") String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "页码不能小于 0") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "每页大小必须大于 0") int size
    ) {
        return OrderPageResponse.from(orderQueryUseCase.search(query, status, page, size));
    }

    @GetMapping("/{id}")
    public OrderDetailResponse getById(@PathVariable String id) {
        return OrderDetailResponse.from(orderQueryUseCase.findById(id));
    }
}
