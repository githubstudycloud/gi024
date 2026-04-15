package com.example.multids.testapp.web;

import java.math.BigDecimal;

/**
 * 订单汇总响应。
 */
public record OrderSummaryResponse(
        long count,
        BigDecimal totalAmount
) {
}
