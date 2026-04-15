package com.example.multids.component.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 多数据源配置属性。
 */
@ConfigurationProperties(prefix = "app.datasource")
public record MultiDataSourceProperties(
        @Name("public") DataSourceEntry publicDataSource,
        List<DataSourceEntry> business
) {

    public MultiDataSourceProperties {
        if (publicDataSource == null) {
            throw new IllegalArgumentException("必须配置公共数据源 app.datasource.public");
        }
        business = business == null ? List.of() : List.copyOf(business);
    }

    /**
     * 单个数据源配置项。
     */
    public record DataSourceEntry(
            String name,
            String url,
            String username,
            String password,
            String driverClassName,
            Topology topology,
            List<String> readReplicas,
            PoolConfig pool,
            String schemaVersion
    ) {

        public DataSourceEntry {
            if (!StringUtils.hasText(name)) {
                throw new IllegalArgumentException("数据源名称不能为空");
            }
            if (!StringUtils.hasText(url)) {
                throw new IllegalArgumentException("数据源 URL 不能为空");
            }
            topology = topology == null ? Topology.STANDALONE : topology;
            readReplicas = readReplicas == null ? List.of() : List.copyOf(readReplicas);
            pool = pool == null ? new PoolConfig(10, 2, 5000) : pool;
            schemaVersion = StringUtils.hasText(schemaVersion) ? schemaVersion : "v1";
        }

        /**
         * 当前版本仅支持 JDBC 数据源，这里提前识别 Mongo 配置并给出明确错误。
         */
        public boolean isMongoStyle() {
            return "mongo".equalsIgnoreCase(driverClassName)
                    || url.toLowerCase().startsWith("mongodb:");
        }
    }

    /**
     * 连接池配置。
     */
    public record PoolConfig(
            int maximumPoolSize,
            int minimumIdle,
            long connectionTimeout
    ) {

        public PoolConfig {
            maximumPoolSize = maximumPoolSize > 0 ? maximumPoolSize : 10;
            minimumIdle = minimumIdle > 0 ? minimumIdle : 2;
            connectionTimeout = connectionTimeout > 0 ? connectionTimeout : 5000;
        }
    }

    /**
     * 数据源拓扑。
     */
    public enum Topology {
        STANDALONE,
        REPLICA,
        CLUSTER
    }
}
