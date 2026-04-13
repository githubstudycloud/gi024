package com.example.mcp.application.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 迁移过程中的特性开关。
 */
@ConfigurationProperties(prefix = "migration.flags")
public class MigrationFlagsProperties {

    /**
     * 是否启用用户域新数据源。
     */
    private boolean userSourceNew;

    /**
     * 是否启用订单域新数据源。
     */
    private boolean orderSourceNew;

    /**
     * 是否启用商品域新数据源。
     */
    private boolean productSourceNew;

    public boolean isUserSourceNew() {
        return userSourceNew;
    }

    public void setUserSourceNew(boolean userSourceNew) {
        this.userSourceNew = userSourceNew;
    }

    public boolean isOrderSourceNew() {
        return orderSourceNew;
    }

    public void setOrderSourceNew(boolean orderSourceNew) {
        this.orderSourceNew = orderSourceNew;
    }

    public boolean isProductSourceNew() {
        return productSourceNew;
    }

    public void setProductSourceNew(boolean productSourceNew) {
        this.productSourceNew = productSourceNew;
    }
}
