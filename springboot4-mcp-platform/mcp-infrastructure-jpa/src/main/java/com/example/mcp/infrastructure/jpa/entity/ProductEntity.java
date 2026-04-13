package com.example.mcp.infrastructure.jpa.entity;

import com.example.mcp.domain.model.ProductRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 新系统中的商品表实体。
 */
@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @Column(length = 64, nullable = false)
    private String id;

    @Column(nullable = false, length = 64)
    private String productCode;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 16)
    private String currency;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private Instant createdAt;

    public static ProductEntity of(ProductRecord record) {
        ProductEntity entity = new ProductEntity();
        entity.setId(record.id());
        entity.setProductCode(record.productCode());
        entity.setName(record.name());
        entity.setCategory(record.category());
        entity.setPrice(record.price());
        entity.setCurrency(record.currency());
        entity.setActive(record.active());
        entity.setCreatedAt(record.createdAt());
        return entity;
    }

    public ProductRecord toDomain() {
        return new ProductRecord(id, productCode, name, category, price, currency, active, createdAt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
