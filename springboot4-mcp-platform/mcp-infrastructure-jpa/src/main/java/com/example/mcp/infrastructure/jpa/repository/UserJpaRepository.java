package com.example.mcp.infrastructure.jpa.repository;

import com.example.mcp.infrastructure.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 新系统用户仓储。
 */
public interface UserJpaRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {

    long countByActiveTrue();
}
