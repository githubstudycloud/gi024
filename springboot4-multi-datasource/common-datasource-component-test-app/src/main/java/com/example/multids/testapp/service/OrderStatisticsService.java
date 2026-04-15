package com.example.multids.testapp.service;

import com.example.multids.component.routing.UseDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 订单库统计服务。
 */
@Service
public class OrderStatisticsService {

    private final JdbcTemplate jdbcTemplate;

    public OrderStatisticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @UseDataSource("order-service")
    @Transactional(readOnly = true)
    public long countOrders() {
        return jdbcTemplate.queryForObject("SELECT COUNT(1) FROM order_record", Long.class);
    }

    @UseDataSource("order-service")
    @Transactional(readOnly = true)
    public BigDecimal totalAmount() {
        return jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(amount), 0) FROM order_record",
                BigDecimal.class
        );
    }
}
