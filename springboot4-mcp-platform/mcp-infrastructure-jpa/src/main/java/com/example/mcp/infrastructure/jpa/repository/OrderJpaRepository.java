package com.example.mcp.infrastructure.jpa.repository;

import com.example.mcp.infrastructure.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 新系统订单仓储。
 */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String>, JpaSpecificationExecutor<OrderEntity> {

    long countByStatus(String status);
}
