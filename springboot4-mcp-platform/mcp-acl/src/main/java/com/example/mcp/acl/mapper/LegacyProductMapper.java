package com.example.mcp.acl.mapper;

import com.example.mcp.acl.dto.LegacyProductItemResponse;
import com.example.mcp.domain.model.ProductRecord;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 负责把旧系统商品 DTO 转成新的领域模型。
 */
@Component
public class LegacyProductMapper {

    private static final DateTimeFormatter LEGACY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ProductRecord toProductRecord(LegacyProductItemResponse source) {
        return new ProductRecord(
                source.id(),
                source.productCode(),
                source.name(),
                source.category(),
                source.price(),
                source.currency(),
                source.active(),
                toInstant(source.createdAt())
        );
    }

    private Instant toInstant(String value) {
        return LocalDateTime.parse(value, LEGACY_TIME_FORMATTER)
                .toInstant(ZoneOffset.UTC);
    }
}
