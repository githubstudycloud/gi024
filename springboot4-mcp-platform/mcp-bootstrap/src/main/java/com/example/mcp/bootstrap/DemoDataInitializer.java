package com.example.mcp.bootstrap;

import com.example.mcp.domain.model.UserProfile;
import com.example.mcp.domain.model.OrderRecord;
import com.example.mcp.infrastructure.jpa.entity.OrderEntity;
import com.example.mcp.infrastructure.jpa.entity.UserEntity;
import com.example.mcp.infrastructure.jpa.repository.OrderJpaRepository;
import com.example.mcp.infrastructure.jpa.repository.UserJpaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 开发环境初始化样板数据，便于直接验证“新系统数据源”路径。
 */
@Configuration
public class DemoDataInitializer {

    @Bean
    @Profile("dev")
    CommandLineRunner seedDemoData(UserJpaRepository userJpaRepository, OrderJpaRepository orderJpaRepository) {
        return args -> {
            if (userJpaRepository.count() > 0 || orderJpaRepository.count() > 0) {
                return;
            }

            List<UserEntity> users = List.of(
                    UserEntity.of(new UserProfile(
                            "u-1001", "alice", "Alice Zhang", "alice@example.com",
                            "ADMIN", true, Instant.parse("2026-04-01T08:00:00Z")
                    )),
                    UserEntity.of(new UserProfile(
                            "u-1002", "bob", "Bob Li", "bob@example.com",
                            "USER", true, Instant.parse("2026-04-02T08:00:00Z")
                    )),
                    UserEntity.of(new UserProfile(
                            "u-1003", "carol", "Carol Wang", "carol@example.com",
                            "AUDITOR", false, Instant.parse("2026-04-03T08:00:00Z")
                    ))
            );

            userJpaRepository.saveAll(users);

            List<OrderEntity> orders = List.of(
                    OrderEntity.of(new OrderRecord(
                            "o-2001", "SO-2026-0001", "Acme Corp", "PENDING",
                            new BigDecimal("2999.50"), "CNY", Instant.parse("2026-04-01T10:00:00Z")
                    )),
                    OrderEntity.of(new OrderRecord(
                            "o-2002", "SO-2026-0002", "Globex Ltd", "PAID",
                            new BigDecimal("1288.00"), "CNY", Instant.parse("2026-04-02T10:00:00Z")
                    )),
                    OrderEntity.of(new OrderRecord(
                            "o-2003", "SO-2026-0003", "Innotech", "PENDING",
                            new BigDecimal("8650.00"), "USD", Instant.parse("2026-04-03T10:00:00Z")
                    ))
            );

            orderJpaRepository.saveAll(orders);
        };
    }
}
