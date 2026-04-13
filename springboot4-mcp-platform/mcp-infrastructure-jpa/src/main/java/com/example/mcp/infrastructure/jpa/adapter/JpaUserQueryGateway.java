package com.example.mcp.infrastructure.jpa.adapter;

import com.example.mcp.domain.common.ActiveUserCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.model.UserProfile;
import com.example.mcp.domain.port.out.NewUserQueryGateway;
import com.example.mcp.infrastructure.jpa.entity.UserEntity;
import com.example.mcp.infrastructure.jpa.repository.UserJpaRepository;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 基于 JPA 的用户查询实现。
 */
@Component
@Observed(name = "new.user.gateway")
public class JpaUserQueryGateway implements NewUserQueryGateway {

    private final UserJpaRepository userJpaRepository;

    public JpaUserQueryGateway(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public PageResult<UserProfile> search(String query, String role, int page, int size) {
        Specification<UserEntity> specification = (root, criteriaQuery, criteriaBuilder) -> {
            String likeValue = "%" + query.toLowerCase() + "%";
            var keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), likeValue),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("displayName")), likeValue),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeValue)
            );
            if (StringUtils.hasText(role)) {
                return criteriaBuilder.and(keywordPredicate, criteriaBuilder.equal(root.get("role"), role));
            }
            return keywordPredicate;
        };

        Page<UserEntity> entityPage = userJpaRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return new PageResult<>(
                entityPage.getContent().stream().map(UserEntity::toDomain).toList(),
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                ServedBy.NEW
        );
    }

    @Override
    public Optional<ItemResult<UserProfile>> findById(String id) {
        return userJpaRepository.findById(id)
                .map(UserEntity::toDomain)
                .map(user -> new ItemResult<>(user, ServedBy.NEW));
    }

    @Override
    public ActiveUserCount countActiveUsers() {
        return new ActiveUserCount(userJpaRepository.countByActiveTrue(), ServedBy.NEW);
    }
}
