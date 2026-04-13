package com.example.mcp.infrastructure.jpa.repository;

import com.example.mcp.infrastructure.jpa.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 新系统商品仓储。
 */
public interface ProductJpaRepository extends JpaRepository<ProductEntity, String>, JpaSpecificationExecutor<ProductEntity> {

    long countByActiveTrue();
}
