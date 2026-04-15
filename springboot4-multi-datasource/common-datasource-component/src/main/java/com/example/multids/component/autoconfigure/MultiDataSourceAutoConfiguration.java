package com.example.multids.component.autoconfigure;

import com.example.multids.component.config.MultiDataSourceProperties;
import com.example.multids.component.factory.DataSourceFactory;
import com.example.multids.component.factory.DefaultDataSourceFactory;
import com.example.multids.component.health.MultiDataSourceHealthContributor;
import com.example.multids.component.registry.DataSourceRegistry;
import com.example.multids.component.routing.DataSourceRoutingAspect;
import com.example.multids.component.routing.TenantRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.contributor.HealthContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 多数据源自动配置。
 */
@AutoConfiguration
@EnableConfigurationProperties(MultiDataSourceProperties.class)
@ConditionalOnClass({DataSource.class, HikariDataSource.class})
public class MultiDataSourceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSourceFactory dataSourceFactory() {
        return new DefaultDataSourceFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceRegistry dataSourceRegistry(
            MultiDataSourceProperties properties,
            DataSourceFactory dataSourceFactory
    ) {
        DataSourceRegistry registry = new DataSourceRegistry();

        registerEntry(registry, dataSourceFactory, properties.publicDataSource());
        properties.business().forEach(entry -> registerEntry(registry, dataSourceFactory, entry));

        return registry;
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(
            MultiDataSourceProperties properties,
            DataSourceRegistry registry
    ) {
        TenantRoutingDataSource router = new TenantRoutingDataSource();
        Map<Object, Object> targets = new LinkedHashMap<>();
        registry.snapshot().forEach(targets::put);
        router.setTargetDataSources(targets);
        router.setDefaultTargetDataSource(registry.resolve(properties.publicDataSource().name()));
        router.afterPropertiesSet();
        return router;
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceRoutingAspect dataSourceRoutingAspect() {
        return new DataSourceRoutingAspect();
    }

    @Bean
    @ConditionalOnMissingBean(name = "multiDataSourceHealthContributor")
    @ConditionalOnClass(HealthContributor.class)
    public MultiDataSourceHealthContributor multiDataSourceHealthContributor(DataSourceRegistry registry) {
        return new MultiDataSourceHealthContributor(registry);
    }

    private void registerEntry(
            DataSourceRegistry registry,
            DataSourceFactory dataSourceFactory,
            MultiDataSourceProperties.DataSourceEntry entry
    ) {
        registry.register(entry.name(), dataSourceFactory.create(entry), entry.topology());
    }
}
