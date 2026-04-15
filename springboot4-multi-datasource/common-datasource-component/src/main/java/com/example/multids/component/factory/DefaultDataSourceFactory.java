package com.example.multids.component.factory;

import com.example.multids.component.config.MultiDataSourceProperties.DataSourceEntry;
import com.example.multids.component.config.MultiDataSourceProperties.PoolConfig;
import com.example.multids.component.routing.ReadWriteSplittingDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

/**
 * 默认数据源工厂。
 */
public class DefaultDataSourceFactory implements DataSourceFactory {

    @Override
    public DataSource create(DataSourceEntry entry) {
        if (entry.isMongoStyle()) {
            throw new UnsupportedOperationException(
                    "当前组件版本仅支持 JDBC 数据源，暂不支持 Mongo 数据源：" + entry.name()
            );
        }

        return switch (entry.topology()) {
            case STANDALONE -> buildStandalone(entry);
            case REPLICA -> buildReplicaAware(entry);
            case CLUSTER -> buildCluster(entry);
        };
    }

    private DataSource buildStandalone(DataSourceEntry entry) {
        return buildHikariDataSource(entry, entry.url(), entry.pool().maximumPoolSize());
    }

    private DataSource buildReplicaAware(DataSourceEntry entry) {
        DataSource primary = buildStandalone(entry);
        List<DataSource> replicas = entry.readReplicas().stream()
                .map(url -> buildHikariDataSource(entry, url, entry.pool().maximumPoolSize()))
                .map(DataSource.class::cast)
                .toList();

        if (replicas.isEmpty()) {
            return primary;
        }
        return new ReadWriteSplittingDataSource(primary, replicas);
    }

    private DataSource buildCluster(DataSourceEntry entry) {
        int maxPoolSize = Math.max(entry.pool().maximumPoolSize(), entry.pool().maximumPoolSize() * 2);
        return buildHikariDataSource(entry, entry.url(), maxPoolSize);
    }

    private DataSource buildHikariDataSource(DataSourceEntry entry, String url, int maxPoolSize) {
        PoolConfig pool = entry.pool();
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setPoolName("pool-" + entry.name() + "-" + Integer.toHexString(url.hashCode()));
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(pool.minimumIdle());
        config.setConnectionTimeout(pool.connectionTimeout());
        if (StringUtils.hasText(entry.driverClassName())) {
            config.setDriverClassName(entry.driverClassName());
        }
        if (StringUtils.hasText(entry.username())) {
            config.setUsername(entry.username());
        }
        if (entry.password() != null) {
            config.setPassword(entry.password());
        }
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }
}
