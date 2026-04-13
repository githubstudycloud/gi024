package com.example.mcp.infrastructure.jpa.entity;

import com.example.mcp.domain.model.OrderRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 新系统中的订单表实体。
 */
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(length = 64, nullable = false)
    private String id;

    @Column(nullable = false, length = 64)
    private String orderNo;

    @Column(nullable = false, length = 128)
    private String customerName;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 16)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    public static OrderEntity of(OrderRecord record) {
        OrderEntity entity = new OrderEntity();
        entity.setId(record.id());
        entity.setOrderNo(record.orderNo());
        entity.setCustomerName(record.customerName());
        entity.setStatus(record.status());
        entity.setAmount(record.amount());
        entity.setCurrency(record.currency());
        entity.setCreatedAt(record.createdAt());
        return entity;
    }

    public OrderRecord toDomain() {
        return new OrderRecord(id, orderNo, customerName, status, amount, currency, createdAt);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
