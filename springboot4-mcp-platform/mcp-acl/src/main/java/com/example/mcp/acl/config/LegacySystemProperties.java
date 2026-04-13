package com.example.mcp.acl.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 旧系统访问配置。
 */
@ConfigurationProperties(prefix = "legacy")
public class LegacySystemProperties {

    private String baseUrl = "http://localhost:8088";
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration readTimeout = Duration.ofSeconds(10);
    private String successCode = "0000";
    private final OrderApi order = new OrderApi();
    private final ProductApi product = new ProductApi();

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Duration readTimeout) {
        this.readTimeout = readTimeout;
    }

    public String getSuccessCode() {
        return successCode;
    }

    public void setSuccessCode(String successCode) {
        this.successCode = successCode;
    }

    public OrderApi getOrder() {
        return order;
    }

    public ProductApi getProduct() {
        return product;
    }

    /**
     * 订单域旧系统接口配置。
     */
    public static class OrderApi {

        private String searchPath = "/legacy-api/orders/page";
        private String detailPath = "/legacy-api/orders/{id}";
        private String pendingCountPath = "/legacy-api/orders/statistics/pending";

        public String getSearchPath() {
            return searchPath;
        }

        public void setSearchPath(String searchPath) {
            this.searchPath = searchPath;
        }

        public String getDetailPath() {
            return detailPath;
        }

        public void setDetailPath(String detailPath) {
            this.detailPath = detailPath;
        }

        public String getPendingCountPath() {
            return pendingCountPath;
        }

        public void setPendingCountPath(String pendingCountPath) {
            this.pendingCountPath = pendingCountPath;
        }
    }

    /**
     * 商品域旧系统接口配置。
     */
    public static class ProductApi {

        private String searchPath = "/legacy-api/products/page";
        private String detailPath = "/legacy-api/products/{id}";
        private String activeCountPath = "/legacy-api/products/statistics/active";

        public String getSearchPath() {
            return searchPath;
        }

        public void setSearchPath(String searchPath) {
            this.searchPath = searchPath;
        }

        public String getDetailPath() {
            return detailPath;
        }

        public void setDetailPath(String detailPath) {
            this.detailPath = detailPath;
        }

        public String getActiveCountPath() {
            return activeCountPath;
        }

        public void setActiveCountPath(String activeCountPath) {
            this.activeCountPath = activeCountPath;
        }
    }
}
