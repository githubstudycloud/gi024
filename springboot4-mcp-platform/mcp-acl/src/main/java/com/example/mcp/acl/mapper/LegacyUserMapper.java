package com.example.mcp.acl.mapper;

import com.example.mcp.acl.dto.LegacyUserItemResponse;
import com.example.mcp.domain.model.UserProfile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 负责把旧系统 DTO 转成新的领域模型。
 */
@Component
public class LegacyUserMapper {

    private static final DateTimeFormatter LEGACY_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserProfile toUserProfile(LegacyUserItemResponse source) {
        return new UserProfile(
                source.id(),
                source.username(),
                source.displayName(),
                source.email(),
                source.role(),
                source.active(),
                toInstant(source.createdAt())
        );
    }

    private Instant toInstant(String value) {
        return LocalDateTime.parse(value, LEGACY_TIME_FORMATTER)
                .toInstant(ZoneOffset.UTC);
    }
}
