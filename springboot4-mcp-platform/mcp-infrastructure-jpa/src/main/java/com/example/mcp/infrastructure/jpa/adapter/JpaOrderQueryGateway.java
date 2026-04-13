package com.example.mcp.infrastructure.jpa.adapter;

import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.PendingOrderCount;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.model.OrderRecord;
import com.example.mcp.domain.port.out.NewOrderQueryGateway;
import com.example.mcp.infrastructure.jpa.entity.OrderEntity;
import com.example.mcp.infrastructure.jpa.repository.OrderJpaRepository;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 基于 JPA 的订单查询实现。
 */
@Component
@Observed(name = "new.order.gateway")
public class JpaOrderQueryGateway implements NewOrderQueryGateway {

    private final OrderJpaRepository orderJpaRepository;

    public JpaOrderQueryGateway(OrderJpaRepository orderJpaRepository) {
        this.orderJpaRepository = orderJpaRepository;
    }

    @Override
    public PageResult<OrderRecord> search(String query, String status, int page, int size) {
        Specification<OrderEntity> specification = (root, criteriaQuery, criteriaBuilder) -> {
            String likeValue = "%" + query.toLowerCase() + "%";
            var keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("orderNo")), likeValue),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("customerName")), likeValue)
            );
            if (StringUtils.hasText(status)) {
                return criteriaBuilder.and(keywordPredicate, criteriaBuilder.equal(root.get("status"), status));
            }
            return keywordPredicate;
        };

        Page<OrderEntity> entityPage = orderJpaRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return new PageResult<>(
                entityPage.getContent().stream().map(OrderEntity::toDomain).toList(),
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                ServedBy.NEW
        );
    }

    @Override
    public Optional<ItemResult<OrderRecord>> findById(String id) {
        return orderJpaRepository.findById(id)
                .map(OrderEntity::toDomain)
                .map(order -> new ItemResult<>(order, ServedBy.NEW));
    }

    @Override
    public PendingOrderCount countPendingOrders() {
        return new PendingOrderCount(orderJpaRepository.countByStatus("PENDING"), ServedBy.NEW);
    }
}
