package com.example.mcp.acl.mapper;

import com.example.mcp.acl.dto.LegacyOrderItemResponse;
import com.example.mcp.domain.model.OrderRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 负责把旧系统订单 DTO 转成新的领域模型。
 */
@Component
public class LegacyOrderMapper {

    private static final DateTimeFormatter LEGACY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public OrderRecord toOrderRecord(LegacyOrderItemResponse source) {
        return new OrderRecord(
                source.id(),
                source.orderNo(),
                source.customerName(),
                source.status(),
                source.amount(),
                source.currency(),
                toInstant(source.createdAt())
        );
    }

    private Instant toInstant(String value) {
        return LocalDateTime.parse(value, LEGACY_TIME_FORMATTER)
                .toInstant(ZoneOffset.UTC);
    }
}
