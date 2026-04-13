package com.example.mcp.infrastructure.jpa.adapter;

import com.example.mcp.domain.common.ActiveProductCount;
import com.example.mcp.domain.common.ItemResult;
import com.example.mcp.domain.common.PageResult;
import com.example.mcp.domain.common.ServedBy;
import com.example.mcp.domain.model.ProductRecord;
import com.example.mcp.domain.port.out.NewProductQueryGateway;
import com.example.mcp.infrastructure.jpa.entity.ProductEntity;
import com.example.mcp.infrastructure.jpa.repository.ProductJpaRepository;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 基于 JPA 的商品查询实现。
 */
@Component
@Observed(name = "new.product.gateway")
public class JpaProductQueryGateway implements NewProductQueryGateway {

    private final ProductJpaRepository productJpaRepository;

    public JpaProductQueryGateway(ProductJpaRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public PageResult<ProductRecord> search(String query, String category, int page, int size) {
        Specification<ProductEntity> specification = (root, criteriaQuery, criteriaBuilder) -> {
            String likeValue = "%" + query.toLowerCase() + "%";
            var keywordPredicate = criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productCode")), likeValue),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likeValue)
            );
            if (StringUtils.hasText(category)) {
                return criteriaBuilder.and(keywordPredicate, criteriaBuilder.equal(root.get("category"), category));
            }
            return keywordPredicate;
        };

        Page<ProductEntity> entityPage = productJpaRepository.findAll(
                specification,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        return new PageResult<>(
                entityPage.getContent().stream().map(ProductEntity::toDomain).toList(),
                entityPage.getNumber(),
                entityPage.getSize(),
                entityPage.getTotalElements(),
                ServedBy.NEW
        );
    }

    @Override
    public Optional<ItemResult<ProductRecord>> findById(String id) {
        return productJpaRepository.findById(id)
                .map(ProductEntity::toDomain)
                .map(product -> new ItemResult<>(product, ServedBy.NEW));
    }

    @Override
    public ActiveProductCount countActiveProducts() {
        return new ActiveProductCount(productJpaRepository.countByActiveTrue(), ServedBy.NEW);
    }
}
